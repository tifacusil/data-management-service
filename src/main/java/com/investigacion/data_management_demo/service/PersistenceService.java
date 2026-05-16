package com.investigacion.data_management_demo.service;

import com.investigacion.data_management_demo.mapper.DeepDisplacementReadingMapper;
import com.investigacion.data_management_demo.mapper.GnssReadingMapper;
import com.investigacion.data_management_demo.mapper.RainfallReadingMapper;
import com.investigacion.data_management_demo.mapper.SoilPressureReadingMapper;
import com.investigacion.data_management_demo.mapper.StrainReadingMapper;
import com.investigacion.data_management_demo.model.DeepDisplacementPayload;
import com.investigacion.data_management_demo.model.Envelope;
import com.investigacion.data_management_demo.model.GnssPayload;
import com.investigacion.data_management_demo.model.RainfallPayload;
import com.investigacion.data_management_demo.model.SoilPressurePayload;
import com.investigacion.data_management_demo.model.StrainPayload;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Service;

import java.sql.SQLRecoverableException;
import java.sql.SQLTransientException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class PersistenceService {
	private final GnssReadingMapper gnssReadingMapper;
	private final RainfallReadingMapper rainfallReadingMapper;
	private final StrainReadingMapper strainReadingMapper;
	private final SoilPressureReadingMapper soilPressureReadingMapper;
	private final DeepDisplacementReadingMapper deepDisplacementReadingMapper;

	public PersistenceService(
			GnssReadingMapper gnssReadingMapper,
			RainfallReadingMapper rainfallReadingMapper,
			StrainReadingMapper strainReadingMapper,
			SoilPressureReadingMapper soilPressureReadingMapper,
			DeepDisplacementReadingMapper deepDisplacementReadingMapper
	) {
		this.gnssReadingMapper = gnssReadingMapper;
		this.rainfallReadingMapper = rainfallReadingMapper;
		this.strainReadingMapper = strainReadingMapper;
		this.soilPressureReadingMapper = soilPressureReadingMapper;
		this.deepDisplacementReadingMapper = deepDisplacementReadingMapper;
	}

	public void persistGnss(Envelope envelope, GnssPayload payload) {
		try {
			gnssReadingMapper.insertIgnore(
					envelope.messageId(),
					envelope.projectId(),
					envelope.sensorId(),
					toUtc(envelope.timestamp()),
					toUtc(envelope.receivedAt()),
					payload.displacementXmm(),
					payload.displacementYmm(),
					payload.displacementZmm(),
					payload.displacementHorizontalMm(),
					payload.cumulativeDisplacementMm(),
					payload.fixQuality()
			);
		} catch (DataAccessException ex) {
			throw wrapDbException(ex);
		}
	}

	public void persistRainfall(Envelope envelope, RainfallPayload payload) {
		try {
			rainfallReadingMapper.insertIgnore(
					envelope.messageId(),
					envelope.projectId(),
					envelope.sensorId(),
					toUtc(envelope.timestamp()),
					toUtc(envelope.receivedAt()),
					payload.rainfallMm(),
					payload.cumulativeRainfallMm(),
					payload.intervalMinutes()
			);
		} catch (DataAccessException ex) {
			throw wrapDbException(ex);
		}
	}

	public void persistStrain(Envelope envelope, StrainPayload payload) {
		try {
			strainReadingMapper.insertIgnore(
					envelope.messageId(),
					envelope.projectId(),
					envelope.sensorId(),
					toUtc(envelope.timestamp()),
					toUtc(envelope.receivedAt()),
					payload.strainMicrostrain(),
					payload.temperatureCelsius()
			);
		} catch (DataAccessException ex) {
			throw wrapDbException(ex);
		}
	}

	public void persistSoilPressure(Envelope envelope, SoilPressurePayload payload) {
		try {
			soilPressureReadingMapper.insertIgnore(
					envelope.messageId(),
					envelope.projectId(),
					envelope.sensorId(),
					toUtc(envelope.timestamp()),
					toUtc(envelope.receivedAt()),
					payload.pressureKpa(),
					payload.temperatureCelsius()
			);
		} catch (DataAccessException ex) {
			throw wrapDbException(ex);
		}
	}

	public void persistDeepDisplacement(Envelope envelope, DeepDisplacementPayload payload) {
		try {
			deepDisplacementReadingMapper.insertIgnore(
					envelope.messageId(),
					envelope.projectId(),
					envelope.sensorId(),
					toUtc(envelope.timestamp()),
					toUtc(envelope.receivedAt()),
					payload.depthM(),
					payload.tiltXmm(),
					payload.tiltYmm(),
					payload.cumulativeTiltMm()
			);
		} catch (DataAccessException ex) {
			throw wrapDbException(ex);
		}
	}

	private RuntimeException wrapDbException(DataAccessException ex) {
		if (ex instanceof TransientDataAccessException || ex instanceof CannotGetJdbcConnectionException || isSqlTransient(ex)) {
			return new RetryableMessageException("Fallo temporal MySQL", ex);
		}
		return new NonRetryableMessageException("Fallo no recuperable MySQL", ex);
	}

	private static boolean isSqlTransient(Throwable t) {
		Throwable current = t;
		while (current != null) {
			if (current instanceof SQLTransientException || current instanceof SQLRecoverableException) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private static LocalDateTime toUtc(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
	}
}
