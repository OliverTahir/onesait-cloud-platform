package com.minsait.onesait.platform.reports.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.reports.dto.ReportDataDto;
import com.minsait.onesait.platform.reports.service.ReportBuilderService;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleExporterInputItem;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@Service
public class ReportBuilderServiceImpl implements ReportBuilderService {

	// TODO: Analizar Visor de Informes --> JasperDesignViewer.viewReportDesign(is, isXML);
	
	// TODO: Analizar cargar NO compilados JRXml
	//			JasperDesign design = JRXmlLoader.load(new ByteArrayInputStream(new byte[] {}));
	
	@Override
	public ReportDataDto generateReport(byte[] bytes, String name, ReportTypeEnum type) throws JRException, IOException {
		
		InputStream is = new ByteArrayInputStream(bytes);
		
		return generateReport(is, name, type);
	}
		
	@Override
	public ReportDataDto generateReport(InputStream is, String name, ReportTypeEnum type) throws JRException, IOException {
		try {
			JasperReport jasperReport = JasperCompileManager.compileReport(is);
			
			//JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);
			return generateReport(jasperReport, name, type);
		} finally {
			is.close();
		}
	}
	
	@Override
	public ReportDataDto generateReport(JasperReport jasperReport, String name, ReportTypeEnum type) throws JRException {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		JasperFillManager.fillReportToStream(jasperReport, outputStream, new HashMap<String, Object>(), new JREmptyDataSource());
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<String, Object>(), new JREmptyDataSource());
		
		byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint);
		
		/////////////////////////////
		JasperExportManager.exportReportToPdfFile(jasperPrint, "D:\\work\\onesait-cloud-platform\\sources\\modules\\control-panel\\src\\main\\resources\\report\\test.pdf");
		/////////////////////////////
		
		
		
		
		/////////////////////////////
		
		return ReportDataDto.builder() //
				.name(name) //
				.extension(type.extension()) //
				.contentType(type.contentType()) //
				.content(bytes) //
				.build();
				
	}
}
