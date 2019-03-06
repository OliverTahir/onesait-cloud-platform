package com.minsait.onesait.platform.reports.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {
		"com.minsait.onesait.platform.reports.converter",
		"com.minsait.onesait.platform.reports.service"
})
public class TestConfig {

}
