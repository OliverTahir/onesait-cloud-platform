package com.minsait.onesait.platform.config.services.reports;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.repository.report.ReportRepository;

@Service
public class ReportServiceImpl implements ReportService {

	@Autowired
	private ReportRepository reportRepository;
	
	@Transactional(value = TxType.SUPPORTS)
	@Override
	public List<Report> findAllActiveReports() {
		
		return reportRepository.findAllActive();
	}
	
	@Transactional(value = TxType.SUPPORTS)
	@Override
	public List<Report> findAllActiveReportsByUserId(String userId) {
		
		return reportRepository.findAllActiveByUserId(userId);
	}

	@Transactional(value = TxType.SUPPORTS)
	@Override
	public Report findById(Long id) {
		
		return reportRepository.findById(id);
	}
	
	@Transactional(value = TxType.REQUIRED)
	@Override
	public void saveOrUpdate(Report report) {
		
		reportRepository.save(report);
	}

	@Transactional(value = TxType.REQUIRED)
	@Override
	public void disable(Long id) {
		
		Report entity = reportRepository.findOne(id);
		
		if (entity != null) {
			entity.setActive(Boolean.FALSE);
			reportRepository.save(entity);
		}
	}
}




//return findAllActiveReportsByUserId("developer");


/*List<Report> reports = new ArrayList<>();
for (int i = 1; i < 101; i++) {
	User user = new User();
	user.setUserId(userId);
	
	Report report = Report.builder()
			.id(Long.valueOf(i))
			.name(i == 1 ? "infbase" : "Informe " + i )
			.description("Description " + i )
			.user(user)
			.created(new Date())
			.isPublic(i%2 == 0 ? true : false)
			.build();
	reports.add(report);
}

return reports;*/