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
package com.minsait.onesait.platform.api.rest.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.service.ApiServiceInterface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ThrottlerProcessor implements Processor {

	@Autowired
	@Qualifier("apiServiceImpl")
	ApiServiceInterface apiService;

	@Override
	public void process(Exchange exchange) throws Exception {
		log.info(exchange.toString());

	}

	public int getThrottle() {
		return 3;
	}

}
