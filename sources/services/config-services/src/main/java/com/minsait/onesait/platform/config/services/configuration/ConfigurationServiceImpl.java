/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.config.services.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.minsait.onesait.platform.config.components.AllConfiguration;
import com.minsait.onesait.platform.config.components.GitlabConfiguration;
import com.minsait.onesait.platform.config.components.GlobalConfiguration;
import com.minsait.onesait.platform.config.components.MailConfiguration;
import com.minsait.onesait.platform.config.components.ModulesUrls;
import com.minsait.onesait.platform.config.components.OpenshiftConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.components.TwitterConfiguration;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.services.exceptions.ConfigServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {
	@Autowired
	private ConfigurationRepository configurationRepository;

	@Override
	public List<Configuration> getAllConfigurations() {
		return configurationRepository.findAll();
	}

	@Override
	public List<Configuration> getAllConfigurations(User user) {
		return configurationRepository.findByUser(user);
	}

	@Override
	@Transactional
	public void deleteConfiguration(String id) {
		configurationRepository.deleteById(id);
	}

	@Override
	public List<Type> getAllConfigurationTypes() {
		final List<Configuration.Type> types = Arrays.asList(Configuration.Type.values());
		return types;

	}

	@Override
	public Configuration getConfiguration(String id) {
		return configurationRepository.findById(id);
	}

	@Override
	public Configuration createConfiguration(Configuration configuration) {
		Configuration oldConfiguration = configurationRepository.findById(configuration.getId());
		if (oldConfiguration != null)
			throw new ConfigServiceException(
					"You cann´t create a Configuration that exists:" + configuration.toString());
		oldConfiguration = configurationRepository.findByTypeAndEnvironmentAndSuffix(configuration.getType(),
				configuration.getEnvironment(), configuration.getSuffix());
		if (oldConfiguration != null)
			throw new ConfigServiceException(
					"Exist a configuration of this type for the environment and suffix:" + configuration.toString());

		oldConfiguration = new Configuration();
		oldConfiguration.setUser(configuration.getUser());
		oldConfiguration.setType(configuration.getType());
		oldConfiguration.setYmlConfig(configuration.getYmlConfig());
		oldConfiguration.setDescription(configuration.getDescription());
		oldConfiguration.setSuffix(configuration.getSuffix());
		oldConfiguration.setEnvironment(configuration.getEnvironment());
		return configurationRepository.save(oldConfiguration);

	}

	@Override
	// FIXME: Check Exception
	public void updateConfiguration(Configuration configuration) {
		final Configuration oldConfiguration = configurationRepository.findById(configuration.getId());
		if (oldConfiguration != null) {
			oldConfiguration.setYmlConfig(configuration.getYmlConfig());
			oldConfiguration.setDescription(configuration.getDescription());
			oldConfiguration.setSuffix(configuration.getSuffix());
			oldConfiguration.setEnvironment(configuration.getEnvironment());
			configurationRepository.save(oldConfiguration);

		} else {
			throw new ConfigServiceException("You cann´t update a Configuration:" + configuration.toString());
		}
	}

	@Override
	public TwitterConfiguration getTwitterConfiguration(String environment, String suffix) {
		try {
			final Configuration config = this.getConfiguration(Configuration.Type.TWITTER, environment, suffix);
			final Constructor constructor = new Constructor(AllConfiguration.class);
			final Yaml yaml = new Yaml(constructor);
			final AllConfiguration tConfig = yaml.loadAs(config.getYmlConfig(), AllConfiguration.class);
			return tConfig.getTwitter();
		} catch (final Exception e) {
			log.error("Error getting TwitterConfiguration", e);
			throw new ConfigServiceException("Error getting TwitterConfiguration", e);
		}
	}

	@Override
	public boolean existsConfiguration(Configuration configuration) {
		if (configurationRepository.findById(configuration.getId()) == null)
			return false;
		else
			return true;
	}

	@Override
	public Map fromYaml(String yaml) {
		final Yaml yamlParser = new Yaml();
		return (Map) yamlParser.load(yaml);
	}

	@Override
	public boolean isValidYaml(final String yml) {
		try {
			final Yaml yamlParser = new Yaml();
			yamlParser.load(yml);
			return true;
		} catch (final Exception e) {
			log.error("Error parsing file:" + e.getMessage());
			return false;
		}
	}

	@Override
	public List<Configuration> getConfigurations(Configuration.Type type) {
		return configurationRepository.findByType(type);
	}

	@Override
	public List<Configuration> getConfigurations(Configuration.Type type, User user) {
		return configurationRepository.findByTypeAndUser(type, user);
	}

	@Override
	public Configuration getConfiguration(Configuration.Type type, String environment, String suffix) {
		if (suffix == null)
			return configurationRepository.findByTypeAndEnvironment(type, environment);
		else
			return configurationRepository.findByTypeAndEnvironmentAndSuffix(type, environment, suffix);
	}

	@Override
	public Configuration getConfigurationByDescription(String description) {
		return configurationRepository.findByDescription(description);
	}

	@Override
	public Urls getEndpointsUrls(String environment) {
		final Configuration config = configurationRepository
				.findByTypeAndEnvironment(Configuration.Type.ENDPOINT_MODULES, environment);
		final Constructor constructor = new Constructor(ModulesUrls.class);
		final Yaml yamlUrls = new Yaml(constructor);
		return yamlUrls.loadAs(config.getYmlConfig(), ModulesUrls.class).getOnesaitplatform().get("urls");

	}

	@Override
	public GitlabConfiguration getGitlabConfiguration(String id) {
		final Configuration config = configurationRepository.findById(id);
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);

		final AllConfiguration allConfig = yaml.loadAs(config.getYmlConfig(), AllConfiguration.class);
		return allConfig.getGitlab();
	}

	@Override
	public RancherConfiguration getRancherConfiguration(String id) {
		final Configuration config = configurationRepository.findById(id);
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);

		final AllConfiguration allConfig = yaml.loadAs(config.getYmlConfig(), AllConfiguration.class);
		return allConfig.getRancher();
	}

	@Override
	public OpenshiftConfiguration getOpenshiftConfiguration(String id) {
		final Configuration config = configurationRepository.findById(id);
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);

		final AllConfiguration allConfig = yaml.loadAs(config.getYmlConfig(), AllConfiguration.class);
		return allConfig.getOpenshift();
	}

	@Override
	public Configuration getConfiguration(Type configurationType, String suffix) {
		return configurationRepository.findByTypeAndSuffixIgnoreCase(configurationType, suffix);
	}

	@Override
	public MailConfiguration getMailConfiguration(String environment) {
		final Configuration configuration = configurationRepository.findByTypeAndEnvironment(Type.MAIL, environment);
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);
		final AllConfiguration allConfig = yaml.loadAs(configuration.getYmlConfig(), AllConfiguration.class);

		return allConfig.getMail();
	}

	@Override
	public RancherConfiguration getRancherConfiguration(String suffix, String environment) {
		final String id = configurationRepository
				.findByTypeAndEnvironmentAndSuffix(Configuration.Type.RANCHER, environment, suffix).getId();
		if (!StringUtils.isEmpty(id))
			return getRancherConfiguration(id);
		return null;
	}

	@Override
	public GlobalConfiguration getGlobalConfiguration(String environment) {
		final Configuration config = configurationRepository.findByTypeAndEnvironment(Type.OPEN_PLATFORM, environment);
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);
		final AllConfiguration allConfig = yaml.loadAs(config.getYmlConfig(), AllConfiguration.class);
		return allConfig.getOnesaitplatform();
	}

}
