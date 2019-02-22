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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.reports.model.ReportDto;
import com.minsait.onesait.platform.reports.service.GenerateReportService;
import com.minsait.onesait.platform.reports.type.ReportTypeEnum;

import net.sf.jasperreports.engine.JRException;

@Controller
public class ReportDownloadController {
	
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private GenerateReportService reportBuilderService;
	
	/**
	 * Cache-Control:private
	 * Connection:keep-alive
	 * Content-Disposition:attachment; filename=Report0.pdf
	 * Content-Length:149851
	 * Content-Type:application/pdf
	 * Date:Wed, 10 Jun 2015 04:17:49 GMT
	 * Server:nginx
	 * Set-Cookie:fileDownload=true; path=/

	 * @param response
	 * @param id
	 * @throws IOException
	 * @throws JRException
	 */
	@GetMapping(value = "/download/report/{id}", produces = { MediaType.APPLICATION_PDF_VALUE })
    public void download(HttpServletResponse response, @PathVariable("id") Long id) throws JRException, IOException {
		
		Report entity = reportService.findById(id);
		
		byte[] bytes = entity.getFile();
		
		// -- PROVISIONAL => ELIMINARRRRRRR
		InputStream is = null;
		try {
			File file = new File("D:\\work\\onesait-cloud-platform\\sources\\modules\\control-panel\\src\\main\\resources\\report\\test.jrxml");
			is = new FileInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// --------------------------------------
		
		//ReportDto reportData = reportBuilderService.generate(is, entity.getName(), ReportTypeEnum.PDF);
		
		//////ReportDto reportData = reportBuilderService.generate(entity, ReportTypeEnum.PDF);
		
		ReportDto reportData = reportBuilderService.generate(entity, ReportTypeEnum.PDF);
		
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
