package com.investigacion.data_management_demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Enumeration;

@SpringBootApplication
@EnableScheduling
public class DataManagementDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataManagementDemoApplication.class, args);
	}

}

@Component
@ConditionalOnProperty(name = "app.consul.enabled", havingValue = "true", matchIfMissing = true)
class ConsulAutoRegistrar {
	private static final Logger log = LoggerFactory.getLogger(ConsulAutoRegistrar.class);

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final URI consulBaseUri;
	private final String serviceName;
	private final String configuredServiceIp;
	private final int servicePort;
	private final String healthPath;
	private final String consulInterval;
	private final String deregisterAfter;

	private volatile String serviceId;
	private volatile Instant lastOkRegister;

	ConsulAutoRegistrar(
			ObjectMapper objectMapper,
			@Value("${app.consul.base-url:http://localhost:8500}") String consulBaseUrl,
			@Value("${app.consul.service-name:data-management}") String serviceName,
			@Value("${app.consul.service-ip:}") String configuredServiceIp,
			@Value("${server.port:8080}") int servicePort,
			@Value("${app.consul.health-path:/health}") String healthPath,
			@Value("${app.consul.interval:10s}") String consulInterval,
			@Value("${app.consul.deregister-after:30s}") String deregisterAfter
	) {
		this.httpClient = HttpClient.newHttpClient();
		this.objectMapper = objectMapper;
		this.consulBaseUri = URI.create(consulBaseUrl);
		this.serviceName = serviceName;
		this.configuredServiceIp = configuredServiceIp;
		this.servicePort = servicePort;
		this.healthPath = healthPath;
		this.consulInterval = consulInterval;
		this.deregisterAfter = deregisterAfter;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		registerOnce(true);
	}

	@Scheduled(fixedDelayString = "${app.consul.heartbeat-ms:10000}")
	public void heartbeat() {
		registerOnce(false);
	}

	@PreDestroy
	public void onShutdown() {
		if (serviceId == null || serviceId.isBlank()) {
			return;
		}
		try {
			URI uri = consulBaseUri.resolve("/v1/agent/service/deregister/" + urlPathEscape(serviceId));
			HttpRequest req = HttpRequest.newBuilder(uri).PUT(HttpRequest.BodyPublishers.noBody()).build();
			HttpResponse<Void> res = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
			if (res.statusCode() / 100 == 2) {
				log.info("Consul deregister ok: {}", serviceId);
			} else {
				log.warn("Consul deregister fail: {} status={}", serviceId, res.statusCode());
			}
		} catch (Exception ex) {
			log.warn("Consul deregister error: {} {}", ex.getClass().getSimpleName(), ex.getMessage());
		}
	}

	private void registerOnce(boolean logOnOk) {
		try {
			String ip = resolveServiceIp(configuredServiceIp);
			String id = buildServiceId(serviceName, ip, servicePort);
			this.serviceId = id;

			ObjectNode root = objectMapper.createObjectNode();
			root.put("ID", id);
			root.put("Name", serviceName);
			root.put("Address", ip);
			root.put("Port", servicePort);

			ObjectNode check = root.putObject("Check");
			check.put("HTTP", "http://" + ip + ":" + servicePort + healthPath);
			check.put("Interval", consulInterval);
			check.put("DeregisterCriticalServiceAfter", deregisterAfter);

			String json = objectMapper.writeValueAsString(root);
			URI uri = consulBaseUri.resolve("/v1/agent/service/register");
			HttpRequest req = HttpRequest.newBuilder(uri)
					.PUT(HttpRequest.BodyPublishers.ofString(json))
					.header("Content-Type", "application/json")
					.build();
			HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			if (res.statusCode() / 100 == 2) {
				lastOkRegister = Instant.now();
				if (logOnOk) {
					log.info("Consul register ok: {} {}:{}", serviceName, ip, servicePort);
				}
				return;
			}
			log.warn("Consul register fail: status={} body={}", res.statusCode(), safeBody(res.body()));
		} catch (Exception ex) {
			Instant last = lastOkRegister;
			if (last == null || last.isBefore(Instant.now().minusSeconds(60))) {
				log.warn("Consul register error: {} {}", ex.getClass().getSimpleName(), ex.getMessage());
			}
		}
	}

	private static String resolveServiceIp(String configured) {
		if (configured != null && !configured.isBlank()) {
			return configured.trim();
		}
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface ni = ifaces.nextElement();
				if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
					continue;
				}
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress a = addrs.nextElement();
					if (a instanceof Inet4Address && !a.isLoopbackAddress() && !a.isLinkLocalAddress()) {
						return a.getHostAddress();
					}
				}
			}
		} catch (Exception ignored) {
		}
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception ex) {
			return "127.0.0.1";
		}
	}

	private static String buildServiceId(String name, String ip, int port) {
		String raw = name + "-" + ip + "-" + port;
		return raw.replaceAll("[^a-zA-Z0-9_.-]", "-");
	}

	private static String urlPathEscape(String s) {
		return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static String safeBody(String body) {
		if (body == null) {
			return "";
		}
		return body.length() > 500 ? body.substring(0, 500) : body;
	}
}
