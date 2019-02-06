package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.reports.dto.ReportDataDto;
import com.minsait.onesait.platform.reports.service.ReportBuilderService;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;

@Controller
public class ReportDownloadController {
	
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private ReportBuilderService reportBuilderService;
	
	@GetMapping(value = "/download/report/{id}")
    public void download(HttpServletResponse response, @PathVariable("id") Long id) throws IOException, JRException {
		
		Report entity = reportService.findById(id);
		
		byte[] bytes = entity.getFile();
		
		//ReportDataDto reportData = reportBuilderService.generateReport(bytes, entity.getName(), ReportTypeEnum.PDF);
		
		OutputStream ouputStream = response.getOutputStream();
		
		InputStream is = new ByteArrayInputStream(entity.getFile());
		JasperReport jasperReport = JasperCompileManager.compileReport(is);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<String, Object>(), new JREmptyDataSource());
		
		JRPdfExporter exporter = new JRPdfExporter(DefaultJasperReportsContext.getInstance());
		exporter.setParameter(JRExporterParameter.JASPER_PRINT,jasperPrint);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, ouputStream);
		
		try {
			exporter.exportReport();
		} catch (JRException e) {
			throw new RuntimeException(e);
		} finally {
//			if (is != null) {
//				is.close();
//			}
			
			if (ouputStream != null) {
				ouputStream.flush();
				ouputStream.close();
			}
		}
		
		response.flushBuffer();
		
		/*if (reportData.getContent() != null) {
			// Hace falta una cookie para que el plugin ajax funcione correctamente y retire la animación de loading...
			Cookie cookie = new Cookie("fileDownload", "true");
			cookie.setPath("/");
			response.addCookie(cookie);
				
			//Preparar response
			response.setHeader("Cache-Control", "max-age=60, must-revalidate");
			response.setHeader("Content-disposition", "attachment; filename=" + reportData.getFullName());
			response.setContentType("application/pdf");	
			response.setContentLength(reportData.getContent().length);
						
			//Enviar fichero al navegador
			response.getOutputStream().write(bytes);
			response.flushBuffer();
		} else{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}*/
	}
	
	
	/*public void export(HttpServletResponse res) throws Exception {
		
		String fileName=this.getExportFileName(req);
		fileName+=".pdf";
		res.setContentType("application/octet-stream");
		res.setHeader("Connection", "close");
		res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
		
		JasperPrint jasperPrint = this.getJasperPrint(req);
		
		
		JRPdfExporter exporter = new JRPdfExporter(DefaultJasperReportsContext.getInstance());
		exporter.setParameter(JRExporterParameter.JASPER_PRINT,jasperPrint);
		
		OutputStream ouputStream = res.getOutputStream();
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, ouputStream);
		
		try {
			exporter.exportReport();
		} catch (JRException e) {
			throw new ServletException(e);
		} finally {
			if (ouputStream != null) {
				ouputStream.flush();
				ouputStream.close();
			}
		}
	}*/
}
