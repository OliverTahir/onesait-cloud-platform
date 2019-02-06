package com.minsait.onesait.platform.reports;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication() // TODO: Revisar scanBasePackages vs @ComponentScan ???
@EnableAutoConfiguration(exclude = { HibernateJpaAutoConfiguration.class })
//@Import(value = { WebMvcConfiguration.class })
@ComponentScan(basePackages = { "com.minsait.onesait.platform.reports" })
public class ReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportApplication.class, args);
	}
}
