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
package com.minsait.onesait.platform.config.services.reports;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.User;

@RunWith(SpringRunner.class)
@SpringBootTest // (classes = { ReportTestConfig.class })
@Transactional
public class ReportServiceTest {

	@Autowired
	ReportService service;

	@Test
	@Commit
	public void test() {

		final User user = new User();
		user.setUserId("developer");

		// final User user = userRepository.findByUserId("developer");

		final Report report = new Report();
		report.setName("TestReport");
		report.setIsPublic(Boolean.TRUE);
		report.setActive(Boolean.TRUE);

		report.setUser(user);

		service.saveOrUpdate(report);
	}

}
