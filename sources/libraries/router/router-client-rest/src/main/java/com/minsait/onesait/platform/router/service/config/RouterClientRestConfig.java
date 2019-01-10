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
package com.minsait.onesait.platform.router.service.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RouterClientRestConfig {
	
	// Determines the timeout in milliseconds until a connection is established.
	@Value("${onesaitplatform.router.client.rest.connect-timeout:30000}")
    private int CONNECT_TIMEOUT;
     
    // The timeout when requesting a connection from the connection manager.
	@Value("${onesaitplatform.router.client.rest.request-timeout:30000}")
    private int REQUEST_TIMEOUT;
     
    // The timeout for waiting for data
	@Value("${onesaitplatform.router.client.rest.socket-timeout:60000}")
    private int SOCKET_TIMEOUT;
 
	@Value("${onesaitplatform.router.client.rest.max-total-connections:100}")
    private int MAX_TOTAL_CONNECTIONS;
	
	@Value("${onesaitplatform.router.client.rest.default-keep-alive-time-millis:20000}")
    private int DEFAULT_KEEP_ALIVE_TIME_MILLIS;
	
	@Value("${onesaitplatform.router.client.rest.close-idle-connection-wait-time-secs:30}")
    private int CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS;

	@Bean
	PoolingHttpClientConnectionManager connectionManager(){
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
		return cm;
	}
	
	@Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return new ConnectionKeepAliveStrategy() {

			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				return DEFAULT_KEEP_ALIVE_TIME_MILLIS;
			}
        };
    }
	
	@Bean
	HttpClient httpClient(){
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext;

		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
					.build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new RuntimeException("Problem configuring SSL verification", e);
		}

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();
		
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
		        .setConnectionManager(connectionManager())
		        .setSSLSocketFactory(csf)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
		
		return httpClient;
	}
	
	@Bean
	public HttpComponentsClientHttpRequestFactory requestFactory() {
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setHttpClient(httpClient());
		return httpRequestFactory;
	}
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate(requestFactory());
	}
	
	@Bean
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager connectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 10000)
            public void run() {
                try {
                    if (connectionManager != null) {
                        log.trace("run IdleConnectionMonitor - Closing expired and idle connections...");
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS, TimeUnit.SECONDS);
                    } else {
                        log.trace("run IdleConnectionMonitor - Http Client Connection manager is not initialised");
                    }
                } catch (Exception e) {
                    log.error("run IdleConnectionMonitor - Exception occurred. msg={}, e={}", e.getMessage(), e);
                }
            }
        };
    }
	
}