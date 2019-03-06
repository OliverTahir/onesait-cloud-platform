package com.minsait.onesait.platform.reports.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.ReportParameter;

@Component
public class ParameterMapConverter {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
	public Map<String, Object> convert(List<ReportParameter> reportParameters) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		for (ReportParameter reportParameter : reportParameters) {

			
			switch (reportParameter.getType()) {
				case INTEGER:
					params.put(reportParameter.getName(), Integer.parseInt(reportParameter.getValue()));
					break;
				case DOUBLE:
					// TODO: REF-DES-001
					params.put(reportParameter.getName(), Double.parseDouble(reportParameter.getValue()));
					break;
				case DATE:
					// TODO: REF-DES-002
					try {
						params.put(reportParameter.getName(), dateFormat.parse(reportParameter.getValue()));
					} catch (ParseException e) {
						throw new RuntimeException(e); // Expecializar
					}
					break;
				
				default: // STRING
					params.put(reportParameter.getName(), reportParameter.getValue());
					break;
			}
		}
	
		return params;
	}
	
	 
	// -------------------------------
}
