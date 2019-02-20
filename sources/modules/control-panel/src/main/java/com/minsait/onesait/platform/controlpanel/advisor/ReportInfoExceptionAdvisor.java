package com.minsait.onesait.platform.controlpanel.advisor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.minsait.onesait.platform.reports.exception.ReportInfoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ReportInfoExceptionAdvisor { // extends ResponseEntityExceptionHandler
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = { ReportInfoException.class })
	public void handleReportInfoException(ReportInfoException e) {
		log.error("Exception al obtener la informacion de la plantilla de Informe", e);
	}
}
