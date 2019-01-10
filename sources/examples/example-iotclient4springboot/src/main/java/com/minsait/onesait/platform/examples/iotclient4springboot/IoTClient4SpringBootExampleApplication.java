
package com.minsait.onesait.platform.examples.iotclient4springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("com.minsait.onesait.platform.examples.iotclient4springboot")
public class IoTClient4SpringBootExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(IoTClient4SpringBootExampleApplication.class, args);
	}

}
