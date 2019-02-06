package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.io.IOException;

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

@Controller
public class JRReportDownloadController {
	
	@Autowired
	ReportService reportService;
	
	@GetMapping(value = "/download/source/{id}")
    public void download(HttpServletResponse response, @PathVariable("id") Long id) throws IOException {
		
		Report entity = reportService.findById(id);
		
		byte[] bytes = entity.getFile();
		
		// -- PROVISIONAL => ELIMINARRRRRRR
		Resource resource = new ClassPathResource("/test/report.pdf");
		bytes = IOUtils.toByteArray(resource.getInputStream());
		// --------------------------------------
		
		if (entity != null && bytes != null) {
			//Hace falta una cookie para que el plugin ajax funcione correctamente y
			//retire la animación de loading...
			Cookie cookie = new Cookie("fileDownload", "true");
			cookie.setPath("/");
			response.addCookie(cookie);
				
			//Preparar response
			response.setHeader("Cache-Control", "max-age=60, must-revalidate");
			response.setHeader("Content-disposition", "attachment; filename=" + entity.getName() + ".pdf");
			response.setContentType("application/pdf");
			response.setContentLength(bytes.length);
						
			//Enviar fichero al navegador
			response.getOutputStream().write(bytes);
			response.flushBuffer();
		} else{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
