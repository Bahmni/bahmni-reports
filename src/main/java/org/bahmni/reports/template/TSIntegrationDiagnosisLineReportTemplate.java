package org.bahmni.reports.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisLineReportConfig;
import org.bahmni.reports.model.TSPageObject;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.webclients.HttpClient;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.columnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;


@UsingDatasource("openmrs")
public class TSIntegrationDiagnosisLineReportTemplate extends BaseReportTemplate<TSIntegrationDiagnosisLineReportConfig> {
    public static final String CREATE_SQL_TEMPLATE = "create temporary table {0}(code varchar(100) not null)";
    public static final String INSERT_SQL_TEMPLATE = "insert into {0} values (?)";
    public static final String DIAGNOSIS_COLUMN_NAME = "Diagnosis";
    public static final String TERMINOLOGY_COLUMN_NAME = "Terminology Code";
    public static final String PATIENT_ID_COLUMN_NAME = "Patient Id";
    public static final String PATIENT_NAME_COLUMN_NAME = "Patient Name";
    public static final String PATIENT_DATE_OF_BIRTH_COLUMN_NAME = "Date of Birth";
    public static final String GENDER_COLUMN_NAME = "Gender";
    public static final String DATE_AND_TIME_COLUMN_NAME = "Date & Time of Diagnosis";
    public static final int TS_DIAGNOSIS_LOOKUP_DEFAULT_PAGE_SIZE = 20;
    private static final Logger logger = LogManager.getLogger(TSIntegrationDiagnosisLineReportTemplate.class);
    private HttpClient httpClient;
    private Properties tsProperties;
    private String descendantsUrlTemplate;

    public TSIntegrationDiagnosisLineReportTemplate(HttpClient httpClient, Properties tsProperties, String descendantsUrlTemplate) {
        super();
        this.httpClient = httpClient;
        this.tsProperties = tsProperties;
        this.descendantsUrlTemplate = descendantsUrlTemplate;
    }

    public void setDescendantsUrlTemplate(String descendantsUrlTemplate) {
        this.descendantsUrlTemplate = descendantsUrlTemplate;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<TSIntegrationDiagnosisLineReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        String tempTableName = "tmpCodes_" + System.nanoTime();
        loadTempTable(connection, tempTableName, report.getConfig().getTerminologyParentCode());
        String sql = getFileContent("sql/tsIntegrationDiagnosisLineReport.sql");

        CommonComponents.addTo(jasperReport, report, pageType);
        jasperReport.addColumn(col.column(PATIENT_ID_COLUMN_NAME, PATIENT_ID_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(PATIENT_NAME_COLUMN_NAME, PATIENT_NAME_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(GENDER_COLUMN_NAME, GENDER_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(PATIENT_DATE_OF_BIRTH_COLUMN_NAME, PATIENT_DATE_OF_BIRTH_COLUMN_NAME, type.dateType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(DIAGNOSIS_COLUMN_NAME, DIAGNOSIS_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        if (report.getConfig().isDisplayTerminologyCode()) {
            String terminologyConfigColumnName = report.getConfig().getTerminologyColumnName();
            String terminologyColumnName = StringUtils.isNotBlank(terminologyConfigColumnName) ? terminologyConfigColumnName : TERMINOLOGY_COLUMN_NAME;
            jasperReport.addColumn(col.column(terminologyColumnName, TERMINOLOGY_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        }
        jasperReport.addColumn(col.column(DATE_AND_TIME_COLUMN_NAME, DATE_AND_TIME_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        String formattedSql = getFormattedSql(sql, report.getConfig().getTsConceptSource(), startDate, endDate, tempTableName);
        jasperReport.setDataSource(formattedSql, connection);

        return new BahmniReportBuilder(jasperReport);
    }

    private void loadTempTable(Connection connection, String tempTableName, String parentCode) {
        int offset = 0;
        int pageSize = getDefaultPageSize();
        try {
            String createSqlStmt = MessageFormat.format(CREATE_SQL_TEMPLATE, tempTableName);
            String insertSqlStmt = MessageFormat.format(INSERT_SQL_TEMPLATE, tempTableName);
            Statement statement = connection.createStatement();
            statement.execute(createSqlStmt);
            PreparedStatement pstmtInsert = connection.prepareStatement(insertSqlStmt);

            TSPageObject pageObject = null;
            do {
                try {
                    pageObject = fetchDescendantsByPagination(parentCode, pageSize, offset, "en");
                } catch (IOException e) {
                    throw new RuntimeException();
                }
                List<String> codes = pageObject.getCodes();
                for (int batchcount = 0; batchcount < codes.size(); batchcount++) {
                    pstmtInsert.setString(1, codes.get(batchcount));
                    pstmtInsert.addBatch();
                }
                pstmtInsert.executeBatch();
                offset += pageSize;
            } while (offset < pageObject.getTotal());
        } catch (SQLException e) {
            logger.error("Error occured while making database call to " + tempTableName + " table");
            throw new RuntimeException();
        }
    }

    private TSPageObject fetchDescendantsByPagination(String terminologyCode, int pageSize, int offset, String localeLanguage) throws IOException {
        String url = MessageFormat.format(descendantsUrlTemplate, terminologyCode, pageSize, offset, localeLanguage);
        String responseStr = httpClient.get(URI.create(url));
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(responseStr, TSPageObject.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }


    private String getFormattedSql(String templateSql, String conceptSourceCode, String startDate, String endDate, String tempTableName) {
        ST sqlTemplate = new ST(templateSql, '#', '#');
        sqlTemplate.add("conceptSourceCode", conceptSourceCode);
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("tempTable", tempTableName);
        return sqlTemplate.render();
    }


    public int getDefaultPageSize() {
        String pageSize = System.getenv("REPORTS_TS_PAGE_SIZE");
        if (pageSize == null) pageSize = tsProperties.getProperty("ts.defaultPageSize");
        if (pageSize != null) {
            return Integer.parseInt(pageSize);
        }
        return TS_DIAGNOSIS_LOOKUP_DEFAULT_PAGE_SIZE;
    }

}
