package com.minsait.onesait.platform.reports.translator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.reports.exception.GenerateReportException;
import com.minsait.onesait.platform.reports.exception.ReportInfoException;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Translator para las excepciones ocurridas al invocar a API. Ver {@link GenerateReportException} y {@link ReportInfoException}.
 * @author aponcep
 *
 */
@Slf4j
@Component
@Aspect
public class ReportExceptionTranslatorAspect {
	
	// -- Pointcut -- //
	@Pointcut("within(com.minsait.onesait.platform.reports.service.impl.GenerateReportServiceImpl)")
	public void generateReport() { }
	
	@Pointcut("within(com.minsait.onesait.platform.reports.service.impl.ReportInfoServiceImpl)")
	public void infoReportTemplate() { }
	
	
	@Around(value = "generateReport()")
	public Object translateGenerateReportException(ProceedingJoinPoint pjp) throws Throwable {
		try {
			return pjp.proceed();
		} catch (Exception e) {
			log.debug("Traducimos la exception: {} al tipo GenerateReportException", e);
			throw new GenerateReportException(e);
		}
	}
	
	@Around(value = "infoReportTemplate()")
	public Object translateReportInfoException(ProceedingJoinPoint pjp) throws Throwable {
		try {
			return pjp.proceed();
		} catch (Exception e) {
			log.debug("Traducimos la exception: {} al tipo ReportInfoException", e);
			throw new ReportInfoException(e);
		}
	}
}
