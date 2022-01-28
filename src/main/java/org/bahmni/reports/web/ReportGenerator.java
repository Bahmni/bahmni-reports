package org.bahmni.reports.web;

import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Reports;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.util.BahmniReportUtil;
import org.bahmni.webclients.HttpClient;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport;

public class ReportGenerator {
    private static final Logger logger = LogManager.getLogger(ReportGenerator.class);
    private static final String EX_MACRO_TEMPLATE_LOCATiON_UNDEFINED = "Can not identify template. Please contact your system administrator.";
    private static final String ERROR_MACRO_TEMPLATE_LOCATION_UNDEFINED = "Can not generate report. Please define the macro template location";
    private static final String EX_INVALID_MACRO_TEMPLATE = "Invalid Template";
    private static final String EX_UNIDENTIFIED_REPORT = "Can not find report specified. Please contact your administrator.";
    private ReportParams reportParams;
    private OutputStream outputStream;
    private AllDatasources allDatasources;
    private BahmniReportsProperties bahmniReportsProperties;
    private HttpClient httpClient;
    private JasperResponseConverter converter;

    public ReportGenerator(ReportParams reportParams, OutputStream outputStream, AllDatasources allDatasources, BahmniReportsProperties bahmniReportsProperties, HttpClient httpClient, JasperResponseConverter converter) {
        this.reportParams = reportParams;
        this.outputStream = outputStream;
        this.allDatasources = allDatasources;
        this.bahmniReportsProperties = bahmniReportsProperties;
        this.httpClient = httpClient;
        this.converter = converter;
    }

    public void invoke() throws Exception {
        ArrayList<AutoCloseable> resources = new ArrayList<>();
        try {
            validateParams();
            Report report = Reports.find(reportParams.getName(), bahmniReportsProperties.getConfigFileUrl(),httpClient);
            validateReport(report);
            report.setHttpClient(httpClient);
            validateResponseTypeSupportedFor(report, reportParams.getResponseType());
            BaseReportTemplate reportTemplate = report.getTemplate(bahmniReportsProperties);
            Connection connection = allDatasources.getConnectionFromDatasource(reportTemplate);
            BahmniReportBuilder reportBuilder = BahmniReportUtil.build(report, connection, reportParams.getStartDate(),
                    reportParams.getEndDate(), resources, reportParams.getPaperSize(), bahmniReportsProperties);
            List<JasperReportBuilder> reports = reportBuilder.getReportBuilders();
            JasperConcatenatedReportBuilder concatenatedReportBuilder = concatenatedReport().concatenate(reports.toArray(new JasperReportBuilder[reports.size()]));
            converter.applyReportTemplates(reports, reportParams.getResponseType());
            converter.convertToResponseType(reportParams, bahmniReportsProperties.getMacroTemplatesTempDirectory(), outputStream, concatenatedReportBuilder);
            resources.add(connection);
        } finally {
            closeResources(resources);
        }
    }

    private void validateReport(Report report) throws UnsupportedEncodingException {
        if (report == null) {
            logger.error(String.format("Invalid report name or definition. Name: %s", reportParams.getName()));
            throw new RuntimeException(EX_UNIDENTIFIED_REPORT);
        }
    }

    private void validateParams() {

        if (!StringUtils.isBlank(reportParams.getMacroTemplateLocation())) {
            String templatePath = bahmniReportsProperties.getMacroTemplatesTempDirectory();
            logger.debug(String.format(" template path: %s", templatePath));
            logger.debug(String.format(" template specified: %s",  reportParams.getMacroTemplateLocation()));

            if (StringUtils.isBlank(templatePath)) {
                logger.error(ERROR_MACRO_TEMPLATE_LOCATION_UNDEFINED);
                throw new RuntimeException(EX_MACRO_TEMPLATE_LOCATiON_UNDEFINED);
            }

            Path templateLocation = Paths.get(templatePath);
            Path normalizedTemplatePath = Paths.get(templatePath, reportParams.getMacroTemplateLocation()).normalize();
            if (!normalizedTemplatePath.startsWith(templateLocation)) {
                logger.error(String.format("Invalid Macro Template Location: %s", reportParams.getMacroTemplateLocation()));
                throw new RuntimeException(EX_INVALID_MACRO_TEMPLATE);
            }
        }
    }

    private void closeResources(ArrayList<AutoCloseable> resources) {
        for (AutoCloseable resource : resources) {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (Exception e) {
                logger.error("Could not close resource", e);
            }
        }
    }

    private void validateResponseTypeSupportedFor(Report report, String responseType) {
        if (report != null && report.getType().equals("concatenated") && responseType.equals("text/csv")) {
            throw new UnsupportedOperationException("CSV format is not supported for Concatenated report");
        }
    }
}
