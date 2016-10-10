package org.bahmni.reports.web;

import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.apache.log4j.Logger;
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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport;

public class ReportGenerator {
    private static final Logger logger = Logger.getLogger(ReportGenerator.class);
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
            Report report = Reports.find(reportParams.getName(), bahmniReportsProperties.getConfigFileUrl(),httpClient);
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
