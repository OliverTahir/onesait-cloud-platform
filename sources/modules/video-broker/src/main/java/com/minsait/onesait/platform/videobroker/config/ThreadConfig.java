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
package com.minsait.onesait.platform.videobroker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ThreadConfig {

	@Value("${onesaitplatform.videobroker.threading.core-pool-size:10}")
	private int corePoolSize;
	@Value("${onesaitplatform.videobroker.threading.max-pool-size:25}")
	private int maxPoolSize;
	@Value("${onesaitplatform.videobroker.threading.queue-capacity:100}")
	private int queueCapacity;
	@Value("${onesaitplatform.videobroker.threading.name-prefix:video_processor}")
	private String threadNamePrefix;

	@Bean
	public TaskExecutor threadPoolTaskExecutor() {
		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setThreadNamePrefix(threadNamePrefix);
		executor.initialize();
		executor.setQueueCapacity(queueCapacity);
		return executor;
	}
}
