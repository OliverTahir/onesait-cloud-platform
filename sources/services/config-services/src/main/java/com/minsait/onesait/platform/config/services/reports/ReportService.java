package com.minsait.onesait.platform.config.services.reports;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Report;

@Service
public interface ReportService {
	
	/**
	 * <p>Find reports by user id (not admin).
	 * @param userId
	 * @return
	 */
	List<Report> findAllActiveReportsByUserId(String userId);

	/**
	 * <p>Find active reports, when user is admin.
	 * @param userId
	 * @return
	 */
	@Secured({ "ROLE_ADMINISTRATOR" })
	List<Report> findAllActiveReports();

	Report findById(Long id);
	
	void saveOrUpdate(Report report);
	
	void disable(Long id);
}
