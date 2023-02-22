package org.bahmni.reports.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SnomedDiagnosisReportConfig;
import org.bahmni.reports.model.TSPageObject;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.webclients.HttpClient;
import org.stringtemplate.v4.ST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.text.MessageFormat;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;


@UsingDatasource("openmrs")
public class SnomedDiagnosisReportTemplate extends BaseReportTemplate<SnomedDiagnosisReportConfig> {
    public static final String CREATE_SQL_TEMPLATE = "create temporary table {0}(code varchar(100) not null)";
    public static final String INSERT_SQL_TEMPLATE = "insert into {0} values (?)";
    private HttpClient httpClient;

    public SnomedDiagnosisReportTemplate(HttpClient httpClient) {
        super();
        this.httpClient = httpClient;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<SnomedDiagnosisReportConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        String tempTableName = "tmpCodes_" + System.nanoTime();
        loadTempTable(connection, tempTableName, report.getConfig().getSnomedParentCode());
        String sql = getFileContent("sql/snomedDiagnosisCount.sql");

        CommonComponents.addTo(jasperReport, report, pageType);
        jasperReport.addColumn(col.column("Diagnosis", "Diagnosis", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column("Snomed Code", "SNOMED Code", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column("Count", "Total", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        String formattedSql = getFormattedSql(sql, startDate, endDate, tempTableName);
        jasperReport.setShowColumnTitle(true).setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL).setDataSource(formattedSql, connection);

        return new BahmniReportBuilder(jasperReport);
    }

    private void loadTempTable(Connection connection, String tempTableName, String parentCode) {
        int offset = 0;
        int pageSize = 10000;
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
                    throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    private TSPageObject fetchDescendantsByPagination(String snomedCode, int pageSize, int offset, String localeLanguage) throws IOException {
        String descendantsUrlTemplate = "http://openmrs:8080/openmrs/ws/rest/v1/terminologyServices/searchSnomedCodes?code={0}&size={1,number,#}&offset={2,number,#}&locale={3}";
        String url = MessageFormat.format(descendantsUrlTemplate, snomedCode, pageSize, offset, localeLanguage);
        //String responseStr = httpClient.get(URI.create(url));
        String responseStr = "";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        BufferedReader br = null;
        if (conn.getResponseCode() == 200) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String strCurrentLine;
            while ((strCurrentLine = br.readLine()) != null) {
                responseStr+=strCurrentLine;
            }
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String strCurrentLine;
            while ((strCurrentLine = br.readLine()) != null) {
                responseStr+=strCurrentLine;
            }
        }
        System.out.println(responseStr);
        ObjectMapper mapper = new ObjectMapper();
        TSPageObject pageObject = null;
        try {
            pageObject = mapper.readValue(responseStr, TSPageObject.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return pageObject;
    }

    private String getFormattedSql(String templateSql, String startDate, String endDate, String tempTableName) {
        ST sqlTemplate = new ST(templateSql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("tempTable", tempTableName);
        return sqlTemplate.render();
    }

}
