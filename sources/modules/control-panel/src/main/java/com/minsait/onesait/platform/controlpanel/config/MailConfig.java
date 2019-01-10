/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.controlpanel.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.minsait.onesait.platform.config.components.MailConfiguration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;

@Configuration
public class MailConfig {

	@Bean("emailSender")
	public JavaMailSender getJavaMailSender() {
		final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		final MailConfiguration configuration = configurationService.getMailConfiguration(DEFAULT_PROFILE);
		mailSender.setHost(configuration.getHost());
		mailSender.setPort(configuration.getPort());

		mailSender.setUsername(configuration.getUsername());
		mailSender.setPassword(configuration.getPassword());

		final Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", configuration.getSmtp().isAuth());
		props.put("mail.smtp.starttls.enable", configuration.getSmtp().isStarttls_enable());
		props.put("mail.smtp.starttls.required", configuration.getSmtp().isStarttls_required());
		props.put("mail.smtp.timeout", configuration.getSmtp().getTimeout());
		props.put("mail.smtp.connectiontimeout", configuration.getSmtp().getConnectiontimeout());
		props.put("mail.smtp.writetimeout", configuration.getSmtp().getWritetimeout());
		return mailSender;
	}

	@Autowired
	private ConfigurationService configurationService;

	private final static String DEFAULT_PROFILE = "default";

}
