package com.minsait.onesait.platform.reports.converter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.ReportExtension;
import com.minsait.onesait.platform.reports.exception.GenerateReportException;
import com.minsait.onesait.platform.reports.model.ReportDto;
import com.minsait.onesait.platform.reports.service.GenerateReportService;
import com.minsait.onesait.platform.reports.service.impl.ParameterMapConverter;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

// TODO: Analizar Visor de Informes --> JasperDesignViewer.viewReportDesign(is, isXML);

// TODO: Analizar cargar NO compilados JRXml
//			JasperDesign design = JRXmlLoader.load(new ByteArrayInputStream(new byte[] {}));

@Service	
@Slf4j
public class GenerateReportServiceImpl implements GenerateReportService {

	@Autowired
	private ParameterMapConverter parameterMapConverter;
	
	@Override
	public ReportDto generate(Report entity, ReportTypeEnum type) {
		
		Map<String, Object> params = parameterMapConverter.convert(entity.getParameters());
		
		byte[] bytes = generate(entity.getFile(), params, entity.getExtension());
		
		return ReportDto.builder() //
				.name(entity.getName()) //
				.extension(type.extension()) // 
				.contentType(type.contentType()) //
				.content(bytes) //
				.build();
	}		

	private byte[] generate(byte[] source, Map<String, Object> params, ReportExtension extension) {
		InputStream is = new ByteArrayInputStream(source);
		
		return generate(is, params, extension);
	}
	
	private byte[] generate(InputStream is, Map<String, Object> params, ReportExtension extension) {
		
		byte[] bytes = null;
		
		switch (extension) {
			case JRXML:
				bytes = generateFromJrXml(is, params);
				break;
				
			case JASPER:
				bytes = generateFromJasper(is, params);
				break;
	
			default:
				throw new GenerateReportException("Unknown extension, must be jrxml or jasper");
		}
		
		return bytes;
	}
		
	private byte[] generateFromJrXml(InputStream is,  Map<String, Object> params) {
		
		try {
			JasperReport jasperReport = JasperCompileManager.compileReport(is);
			
			return generateFromReport(jasperReport, params);
		} catch (JRException e) {
			log.error("Handle unchecked exception", e);
			throw new GenerateReportException(e);
		}
	}

	private byte[] generateFromJasper(InputStream is,  Map<String, Object> params) {
		
		try {
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);
			
			return generateFromReport(jasperReport, params);
		} catch (JRException e) {
			log.error("Handle unchecked exception", e);
			throw new GenerateReportException(e);
		}
	}

	private byte[] generateFromReport(JasperReport jasperReport, Map<String, Object> params) throws JRException {
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params);
		
		return JasperExportManager.exportReportToPdf(jasperPrint);
	}
}
