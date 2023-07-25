package org.bahmni.reports.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.model.TSIntegrationDiagnosisLineReportConfig;
import org.bahmni.reports.model.TSPageObject;
import org.bahmni.webclients.HttpClient;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.columnStyle;

public interface TSIntegrationDiagnosisService {
    final static String CREATE_SQL_TEMPLATE = "create temporary table {0}(code varchar(100) not null)";
    final static String INSERT_SQL_TEMPLATE = "insert into {0} values (?)";
    final static int TS_DIAGNOSIS_LOOKUP_DEFAULT_PAGE_SIZE = 20;
    Logger logger = LogManager.getLogger(TSIntegrationDiagnosisService.class);

    static List<String> getPatientAttributes(TSIntegrationDiagnosisLineReportConfig config) {
        return config.getPatientAttributes() != null ? config.getPatientAttributes() : new ArrayList<>();
    }

    static List<String> getPatientAddresses(TSIntegrationDiagnosisLineReportConfig config) {
        return config.getPatientAddresses() != null ? config.getPatientAddresses() : new ArrayList<>();
    }


    default void loadTempTable(Connection connection, String tempTableName, String parentCode, Properties tsProperties, String descendantsUrlTemplate, HttpClient httpClient) {
        int offset = 0;
        int pageSize = getDefaultPageSize(tsProperties);
        try {
            String createSqlStmt = MessageFormat.format(CREATE_SQL_TEMPLATE, tempTableName);
            String insertSqlStmt = MessageFormat.format(INSERT_SQL_TEMPLATE, tempTableName);
            Statement statement = connection.createStatement();
            statement.execute(createSqlStmt);
            try (PreparedStatement pstmtInsert = connection.prepareStatement(insertSqlStmt)) {

                TSPageObject pageObject = null;
                do {
                    pageObject = fetchDescendantsByPagination(parentCode, pageSize, offset, "en", httpClient, descendantsUrlTemplate);
                    List<String> codes = pageObject.getCodes();
                    for (int batchcount = 0; batchcount < codes.size(); batchcount++) {
                        pstmtInsert.setString(1, codes.get(batchcount));
                        pstmtInsert.addBatch();
                    }
                    pstmtInsert.executeBatch();
                    offset += pageSize;
                } while (offset < pageObject.getTotal());
            }
        } catch (SQLException | IOException e) {
            logger.error("Error occurred while making database call to " + tempTableName + " table");
            throw new RuntimeException(e);
        }
    }

    default TSPageObject fetchDescendantsByPagination(String terminologyCode, int pageSize, int offset, String localeLanguage, HttpClient httpClient, String descendantsUrlTemplate) throws IOException {
        String url = MessageFormat.format(descendantsUrlTemplate, terminologyCode, pageSize, offset, localeLanguage);
        String responseStr = httpClient.get(URI.create(url));
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(responseStr, TSPageObject.class);
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while converting response to page object in fetchDescendantsByPagination");
            throw new RuntimeException(e);
        }
    }

    default int getDefaultPageSize(Properties tsProperties) {
        String pageSize = System.getenv("REPORTS_TS_PAGE_SIZE");
        if (pageSize == null) pageSize = tsProperties.getProperty("ts.defaultPageSize");
        if (pageSize != null) {
            return Integer.parseInt(pageSize);
        }
        return TS_DIAGNOSIS_LOOKUP_DEFAULT_PAGE_SIZE;
    }

    default void createAndAddPatientAddressColumns(JasperReportBuilder jasperReport, TSIntegrationDiagnosisLineReportConfig config) {
        for (String address : getPatientAddresses(config)) {
            TextColumnBuilder<String> column = col.column(address, address, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    default void createAndAddPatientAttributeColumns(JasperReportBuilder jasperReport, TSIntegrationDiagnosisLineReportConfig config) {
        for (String patientAttribute : getPatientAttributes(config)) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    default String constructPatientAddressesToDisplay(TSIntegrationDiagnosisLineReportConfig config) {
        List<String> patientAddresses = getPatientAddresses(config);
        StringBuilder stringBuilder = new StringBuilder();
        if (patientAddresses != null) {
            for (String address : patientAddresses) {
                stringBuilder.append("paddress").append(".").append(address).append(", ");
            }
        }
        return stringBuilder.toString();
    }
}
