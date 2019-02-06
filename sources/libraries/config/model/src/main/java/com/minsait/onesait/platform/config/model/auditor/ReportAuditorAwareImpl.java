package com.minsait.onesait.platform.config.model.auditor;

import org.springframework.data.domain.AuditorAware;

public class ReportAuditorAwareImpl implements AuditorAware<String> {

	@Override
	public String getCurrentAuditor() {
		
		//return SecurityContextHolder.getContext().getAuthentication().getName();
		
		return "Test";
	}

}
