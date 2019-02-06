package com.minsait.onesait.platform.reports;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import lombok.Setter;

@Configuration
@PropertySource("classpath:report.yml")
@ConfigurationProperties(prefix = "onesaitplatform.reports")
@Getter
@Setter
public class ApplicationProperties {

	
}
