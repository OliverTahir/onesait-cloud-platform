package com.minsait.onesait.platform.config.services.reports;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.repository.report.ReportRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

	@Autowired
	private ReportRepository reportRepository;
	
	@Transactional(value = TxType.SUPPORTS)
	@Override
	public List<Report> findAllActiveReports() {
		log.debug("INI. Find active reports (Admin user)");
		return reportRepository.findAllActive();
	}
	
	@Transactional(value = TxType.SUPPORTS)
	@Override
	public List<Report> findAllActiveReportsByUserId(String userId) {
		log.debug("INI. Find active reports by userId: {}", userId);
		return reportRepository.findAllActiveByUserId(userId);
	}

	@Transactional(value = TxType.SUPPORTS)
	@Override
	public Report findById(Long id) {
		log.debug("INI. Find report by Id: {}", id);
		return reportRepository.findById(id);
	}
	
	@Transactional(value = TxType.REQUIRED)
	@Override
	public void saveOrUpdate(Report report) {
		log.debug("INI. Save report: {}", report);
		reportRepository.save(report);
	}

	@Transactional(value = TxType.REQUIRED)
	@Override
	public void disable(Long id) {
		log.debug("INI. Disable report id: {}", id);
		Report entity = reportRepository.findOne(id);
		
		if (entity != null) {
			log.debug("Disable > Find report {}", entity);
			entity.setActive(Boolean.FALSE);
			reportRepository.save(entity);
		}
	}
}