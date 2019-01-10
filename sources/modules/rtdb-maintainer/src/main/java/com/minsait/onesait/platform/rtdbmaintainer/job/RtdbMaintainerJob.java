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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.rtdbmaintainer.service.RtdbExportDeleteService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RtdbMaintainerJob {
	@Value("${onesaitplatform.database.elasticsearch.database:onesaitplatform_rtdb_es}")
	private String onesaitplatform_rtdb_es;
	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	private String onesaitplatform_rtdb;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private RtdbExportDeleteService rtdbExportDeleteService;
	private final static int CORE_POOL_SIZE = 10;
	private final static int MAXIMUM_THREADS = 15;
	private final static long KEEP_ALIVE = 20;
	private final static long DEFAULT_TIMEOUT = 10;

	public void execute(JobExecutionContext context) throws InterruptedException {

		final List<Ontology> ontologies = ontologyService.getCleanableOntologies().stream()
				.filter(o -> o.getRtdbCleanLapse().getMilliseconds() > 0).collect(Collectors.toList());

		if (ontologies.size() > 0) {

			final TimeUnit timeUnit = (TimeUnit) context.getJobDetail().getJobDataMap().get("timeUnit");
			long timeout = context.getJobDetail().getJobDataMap().getLongValue("timeout");
			if (timeout == 0)
				timeout = DEFAULT_TIMEOUT;

			final BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(ontologies.size());
			final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_THREADS, KEEP_ALIVE,
					TimeUnit.SECONDS, blockingQueue);

			final List<CompletableFuture<String>> futureList = ontologies.stream()
					.map(o -> CompletableFuture.supplyAsync(() -> {
						final String query = rtdbExportDeleteService.performExport(o);
						rtdbExportDeleteService.performDelete(o, query);
						return query;
					}, executor)).collect(Collectors.toList());

			final CompletableFuture<Void> globalResut = CompletableFuture
					.allOf(futureList.toArray(new CompletableFuture[futureList.size()]));

			try {

				globalResut.get(timeout, timeUnit);

			} catch (ExecutionException | RuntimeException e) {
				log.error("Error while trying to export and delete ontologies");
				e.printStackTrace();
			} catch (final TimeoutException e) {
				log.error("Timeout Exception while executing batch job Rtdb Maintainer ");
			}
		}

	}

}
