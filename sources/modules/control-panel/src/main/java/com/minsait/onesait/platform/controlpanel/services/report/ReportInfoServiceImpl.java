/**
 * Copyright minsait by Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.controlpanel.services.report;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportField;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameter;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameterType;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportTypeEnum;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Slf4j
public class ReportInfoServiceImpl implements ReportInfoService {

	// -- Filter -- //
	private final Predicate<JRParameter> filterSystemParameters = parameter -> !parameter.isSystemDefined()
			&& parameter.isForPrompting();

	private final Predicate<JRParameter> filterDatasourceParameter = parameter -> "net.sf.jasperreports.json.source"
			.equals(parameter.getName()) && !parameter.isForPrompting();

	@Override
	public ReportInfoDto extract(InputStream is, ReportExtension reportExtension) {
		ReportInfoDto reportInfo = null;

		switch (reportExtension) {
		case JRXML:
			reportInfo = extractFromJrxml(is, reportExtension);
			break;

		case JASPER:
			reportInfo = extractFromJasper(is, reportExtension);
			break;

		default:
			throw new GenerateReportException("Unknown extension, must be jrxml or jasper");
		}

		return reportInfo;
	}

	private ReportInfoDto extractFromJrxml(InputStream is, ReportExtension reportExtension) {
		try {

			final JasperReport report = JasperCompileManager.compileReport(is);

			return extractFromReport(report);

		} catch (final JRException e) {
			throw new ReportInfoException(e);
		}
	}

	private ReportInfoDto extractFromJasper(InputStream is, ReportExtension reportExtension) {
		try {

			final JasperReport report = (JasperReport) JRLoader.loadObject(is);

			return extractFromReport(report);

		} catch (final JRException e) {
			throw new ReportInfoException(e);
		}
	}

	private ReportInfoDto extractFromReport(JasperReport report) {
		log.debug("INI. Extract data from report: {}", report.getName());

		final List<ReportParameter> parameters = report.getParameters() != null
				? Arrays.stream(report.getParameters()).filter(filterSystemParameters)
						.map(p -> ReportParameter.builder().name(p.getName()).description(p.getDescription())
								.type(ReportParameterType.fromJavaType(p.getValueClass().getName())).build())
						.collect(Collectors.toList())
				: new ArrayList<>();
		final List<ReportField<?>> fields = report.getFields() != null
				? Arrays.stream(report.getFields()).map(f -> ReportField.builder().name(f.getName())
						.description(f.getDescription()).type(f.getValueClass()).build()).collect(Collectors.toList())
				: new ArrayList<>();

		final String dataSource = report.getParameters() != null
				? Arrays.stream(report.getParameters()).filter(filterDatasourceParameter).map(parameter -> {
					return parameter.getDefaultValueExpression() != null
							? parameter.getDefaultValueExpression().getText()
							: "";
				}).findFirst().orElse("")
				: "";

		return ReportInfoDto.builder().parameters(parameters).fields(fields).dataSource(dataSource).build();
	}

	@Override
	public byte[] generate(Report entity, ReportTypeEnum type, Map<String, Object> parameters) {

		// final Map<String, Object> params = parameterMapConverter.convert(parameters);
		// TODO retrieve parameters
		return generate(entity.getFile(), parameters, entity.getExtension());

	}

	private byte[] generate(byte[] source, Map<String, Object> params, ReportExtension extension) {
		final InputStream is = new ByteArrayInputStream(source);

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

	private byte[] generateFromJrXml(InputStream is, Map<String, Object> params) {

		try {
			final JasperReport jasperReport = JasperCompileManager.compileReport(is);

			return generateFromReport(jasperReport, params);
		} catch (final JRException e) {
			log.error("Handle unchecked exception", e);
			throw new GenerateReportException(e);
		}
	}

	private byte[] generateFromJasper(InputStream is, Map<String, Object> params) {

		try {
			final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);

			return generateFromReport(jasperReport, params);
		} catch (final JRException e) {
			log.error("Handle unchecked exception", e);
			throw new GenerateReportException(e);
		}
	}

	private byte[] generateFromReport(JasperReport jasperReport, Map<String, Object> params) throws JRException {

		final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

		return JasperExportManager.exportReportToPdf(jasperPrint);
	}
}