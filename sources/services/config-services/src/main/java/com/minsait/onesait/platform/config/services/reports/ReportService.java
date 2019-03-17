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

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Report;

@Service
public interface ReportService {

	/**
	 * <p>
	 * Find reports by user id (not admin).
	 * 
	 * @param userId
	 * @return
	 */
	List<Report> findAllActiveReportsByUserId(String userId);

	/**
	 * <p>
	 * Find active reports, when user is admin.
	 * 
	 * @param userId
	 * @return
	 */
	@Secured({ "ROLE_ADMINISTRATOR" })
	List<Report> findAllActiveReports();

	Report findById(String id);

	void saveOrUpdate(Report report);

	void disable(String id);
}
