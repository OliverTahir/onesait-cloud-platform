package com.minsait.onesait.platform.reports.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.ReportParameter;
import com.minsait.onesait.platform.reports.model.ReportDto;
import com.minsait.onesait.platform.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.reports.service.GenerateReportService;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service
public class GenerateReportServiceImpl implements GenerateReportService {

	// TODO: Analizar Visor de Informes --> JasperDesignViewer.viewReportDesign(is, isXML);
	
	// TODO: Analizar cargar NO compilados JRXml
	//			JasperDesign design = JRXmlLoader.load(new ByteArrayInputStream(new byte[] {}));
	
	@Override
	public ReportDto generate(byte[] bytes, String name, ReportTypeEnum type) throws JRException, IOException {
		
		InputStream is = new ByteArrayInputStream(bytes);
		
		return generate(is, name, type);
	}
		
	@Override
	public ReportDto generate(InputStream is, String name, ReportTypeEnum type) throws JRException, IOException {
		try {
			JasperReport jasperReport = JasperCompileManager.compileReport(is);
			
			//jasperReport = (JasperReport) JRLoader.loadObject(is);
			return generate(jasperReport, name, type);
		} finally {
			is.close();
		}
	}
	
	@Override
	public ReportDto generate(JasperReport jasperReport, String name, ReportTypeEnum type) throws JRException {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		//JsonDataSource datasource = new JsonDataSource
		
		JasperFillManager.fillReportToStream(jasperReport, outputStream, new HashMap<String, Object>());
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<String, Object>());
		
		byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint);
		
		/////////////////////////////
		JasperExportManager.exportReportToPdfFile(jasperPrint, "D:\\tmp\\report\\test.pdf");
		/////////////////////////////
		
		
		
		
		/////////////////////////////
		
		return ReportDto.builder() //
				.name(name) //
				.extension(type.extension()) //
				.contentType(type.contentType()) //
				.content(bytes) //
				.build();
				
	}
	
	@Override
	public ReportDto generate(Report entity, ReportTypeEnum pdf) throws JRException {
		
		InputStream is = new ByteArrayInputStream(entity.getFile());
		
		JasperReport jasperReport = JasperCompileManager.compileReport(is);
		
		List<ReportParameter> parameters = entity.getParameters();
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		for (ReportParameter parameter : parameters) {
			params.put(parameter.getName(), parameter.getValue());
		}
				
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		JasperFillManager.fillReportToStream(jasperReport, outputStream, params);
		
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params);
		
		byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint);
		
		/////////////////////////////
		JasperExportManager.exportReportToPdfFile(jasperPrint, "D:\\tmp\\report\\test.pdf");
		/////////////////////////////
		
		try {
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/////////////////////////////
		
		return ReportDto.builder() //
				.name(entity.getName()) //
				.extension("pdf") // TODO
				.contentType("application/pdf") //
				.content(bytes) //
				.build();
	}

}
