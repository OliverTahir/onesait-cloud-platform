package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.services.reports.ReportService;

import net.sf.jasperreports.engine.JRException;

@Controller
public class ReportTemplateDownloadController {
	
	@Autowired
	private ReportService reportService;
	
	/**
	 * Cache-Control:private
	 * Connection:keep-alive
	 * Content-Disposition:attachment; filename=Report0.jrxml
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
	@GetMapping(value = "/download/report-design/{id}", produces = { MediaType.APPLICATION_PDF_VALUE })
    public void download(HttpServletResponse response, @PathVariable("id") Long id) throws IOException, JRException {
		
		Report entity = reportService.findById(id);
		
		byte[] bytes = entity.getFile();

		if (entity.getFile() == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		// Hace falta una cookie para que el plugin ajax funcione correctamente y retire la animación de loading...
		Cookie cookie = new Cookie("fileDownload", "true");
		cookie.setPath("/");
		response.addCookie(cookie);
		
		//Preparar response
		response.setHeader("Cache-Control", "max-age=60, must-revalidate");
		response.setHeader("Content-disposition", "attachment; filename=" + entity.getName() + "." + entity.getExtension().valueOf());
		response.setContentType("application/jrxml");
		response.setContentLength(bytes.length);
					
		//Enviar fichero al navegador
		response.getOutputStream().write(bytes);
		response.flushBuffer();
	}
}
