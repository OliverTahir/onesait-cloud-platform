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
package com.minsait.onesait.platform.persistence.external.api.rest.client;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import lombok.extern.slf4j.Slf4j;

@Component("APIRestClientImpl")
@Lazy
@Slf4j
public class APIRestClientImpl implements APIRestClient {

	private final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

	private RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		this.restTemplate.setRequestFactory(getRestTemplateRequestFactory());
	}

	@Override
	public APIRestResponse invokeGet(String baseUrl, Optional<String> operation, Optional<List<String>> pathParams,
			Optional<Map<String, Object>> queryParams) {

		// TODO Include headers here
		HttpHeaders headers = new HttpHeaders();
		// headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);

		if (operation.isPresent()) {
			builder.path(operation.get());
		}

		if (pathParams.isPresent()) {
			for (String param : pathParams.get()) {
				builder.path(param);
			}
		}

		if (queryParams.isPresent()) {
			for (Entry<String, Object> queryParam : queryParams.get().entrySet()) {
				builder.queryParam(queryParam.getKey(), queryParam.getValue());
			}
		}

		HttpEntity<?> entity = new HttpEntity<>(headers);

		APIRestResponse resp = new APIRestResponse();
		try {
			HttpEntity<String> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, entity,
					String.class);

			resp.setBody(response.getBody());
			resp.setResponse(HttpStatus.OK.value());

		} catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
			if (HttpStatus.NOT_FOUND.equals(httpClientOrServerExc.getStatusCode())) {
				resp.setResponse(httpClientOrServerExc.getStatusCode().value());
			}
		}

		return resp;
	}

	@Override
	public APIRestResponse invokePut(String baseUrl, Optional<String> operation, Optional<List<String>> pathParams,
			Optional<Map<String, Object>> queryParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public APIRestResponse invokePost(String baseUrl, Optional<String> operation, Optional<List<String>> pathParams,
			Optional<Map<String, Object>> queryParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public APIRestResponse invokeDelete(String baseUrl, Optional<String> operation, Optional<List<String>> pathParams,
			Optional<Map<String, Object>> queryParams) {
		// TODO Auto-generated method stub
		return null;
	}

	private ClientHttpRequestFactory getRestTemplateRequestFactory() {
		RequestConfig config = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
				.setConnectionRequestTimeout(TIMEOUT).build();

		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		return new HttpComponentsClientHttpRequestFactory(client);
	}

}
