package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.reports.dto.ReportDataDto;
import com.minsait.onesait.platform.reports.service.ReportBuilderService;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;

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
		
		// -- PROVISIONAL => ELIMINARRRRRRR
//		File file = new File("D:\\work\\onesait-cloud-platform\\sources\\modules\\control-panel\\src\\main\\resources\\report\\test.jrxml");
//		InputStream is = new FileInputStream(file);
		
		//Resource resource = new ClassPathResource("report/test.jasper");
		//bytes = IOUtils.toByteArray(resource.getInputStream());
		// --------------------------------------
		
		ReportDataDto reportData = reportBuilderService.generateReport(bytes, entity.getName(), ReportTypeEnum.PDF);
		
		if (reportData.getContent() != null) {
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
		}
	}
}
