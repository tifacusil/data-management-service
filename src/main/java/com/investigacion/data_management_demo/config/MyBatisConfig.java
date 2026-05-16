package com.investigacion.data_management_demo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.investigacion.data_management_demo.mapper")
public class MyBatisConfig {
}
