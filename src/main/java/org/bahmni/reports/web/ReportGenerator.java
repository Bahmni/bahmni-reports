package org.bahmni.reports.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.builder.BahmniJasperReportBuilder;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport;
import static org.bahmni.reports.filter.JasperResponseConverter.APPLICATION_JSON;

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
            Report report = Reports.find(reportParams.getName(), bahmniReportsProperties.getConfigFileUrl(), httpClient);
            report.setHttpClient(httpClient);
            validateResponseTypeSupportedFor(report, reportParams.getResponseType());
            BaseReportTemplate reportTemplate = report.getTemplate(bahmniReportsProperties);
            Connection connection = allDatasources.getConnectionFromDatasource(reportTemplate);
            BahmniReportBuilder reportBuilder = BahmniReportUtil.build(report, connection, reportParams.getStartDate(),
                    reportParams.getEndDate(), resources, reportParams.getPaperSize(), bahmniReportsProperties);
            List<JasperReportBuilder> reports = reportBuilder.getReportBuilders();
            JasperConcatenatedReportBuilder concatenatedReportBuilder = concatenatedReport().concatenate(reports.toArray(new JasperReportBuilder[reports.size()]));
            converter.applyReportTemplates(reports, reportParams.getResponseType());

            if (reportParams.getResponseType().equals(APPLICATION_JSON)) {
                convertToJson(reports, outputStream);
            } else {
                converter.convertToResponseType(reportParams, bahmniReportsProperties.getMacroTemplatesTempDirectory(), outputStream, concatenatedReportBuilder);
            }
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

    private void convertToJson(List<JasperReportBuilder> reports, OutputStream outputStream) {
        Map<String, List<Map<String, Object>>> reportsMap = new HashMap<>();
        for (JasperReportBuilder report : reports) {
            try {
                BahmniJasperReportBuilder reportBuilder = (BahmniJasperReportBuilder) report;
                ResultSet rs = reportBuilder.getResultSetDataSource();
                if (null == rs){
                    Connection connection = reportBuilder.getConnection();
                    String sql = reportBuilder.getSql();
                    if (null != connection && null != sql){
                        rs = connection.createStatement().executeQuery(sql);
                    }
                }
                List<Map<String, Object>> reportData = convertResultSetToJSON(rs);
                String reportName = report.getReport().getReportName();
                reportsMap.put(reportName, reportData);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(reportsMap);
            outputStream.write(bytes);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Map<String, Object>> convertResultSetToJSON(ResultSet rs) throws SQLException {
        ArrayList<Map<String, Object>> reportData = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            int numColumns = rsmd.getColumnCount();
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= numColumns; i++) {
                String columnName = rsmd.getColumnLabel(i);
                map.put(columnName, rs.getObject(columnName));
            }
            reportData.add(map);
        }
        return reportData;
    }

}
