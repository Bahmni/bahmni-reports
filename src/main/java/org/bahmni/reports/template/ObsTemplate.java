package org.bahmni.reports.template;


import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.*;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.stringtemplate.v4.ST;

import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class ObsTemplate extends BaseReportTemplate<ObsTemplateConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public ObsTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsTemplateConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        String templateName = report.getConfig().getTemplateName();


        ConnectionDetails connectionDetails = new ConnectionDetails(bahmniReportsProperties.getOpenmrsRootUrl() + "/session", bahmniReportsProperties.getOpenmrsServiceUser(),
                bahmniReportsProperties.getOpenmrsServicePassword(), bahmniReportsProperties.getOpenmrsConnectionTimeout(), bahmniReportsProperties.getOpenmrsReplyTimeout());
        HttpClient httpClient = new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));

        List<ConceptDetails> conceptDetails = null;
        try {
            String url = bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/leafConcepts?conceptName=" + URLEncoder.encode(templateName, "UTF-8");
            String response = null;
            response = httpClient.get(new URI(url));

            ObjectMapper objectMapper = new ObjectMapper();
            conceptDetails = objectMapper.readValue(response, new TypeReference<List<ConceptDetails>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> conceptNames = new ArrayList<>();
        for (ConceptDetails conceptDetail : conceptDetails) {
            conceptNames.add("'" + conceptDetail.getFullName() + "'");
        }

        String conceptNameInClause = StringUtils.join(conceptNames, ",");
        String sql = getFormattedSql(getFileContent("sql/obsTemplate.sql"), report.getConfig(), conceptNameInClause, startDate, endDate);

        TextColumnBuilder<String> patientColumn = col.column("Patient ID", "identifier", type.stringType());
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "patient_name", type.stringType());
        TextColumnBuilder<String> patientGenderColumn = col.column("Gender", "gender", type.stringType());
        TextColumnBuilder<String> patientAgeColumn = col.column("Age", "age", type.stringType());
        TextColumnBuilder<String> providerColumn = col.column("User", "provider_name", type.stringType());
        TextColumnBuilder<String> encounterColumn = col.column("Encounter DateTime", "encounter_datetime", type.stringType());

        jasperReport.columns(patientColumn, patientNameColumn, patientGenderColumn, patientAgeColumn, providerColumn, encounterColumn);

        for (ConceptDetails concept : conceptDetails) {
            TextColumnBuilder<String> column = col.column(concept.getName(), concept.getFullName(), type.stringType());
            jasperReport.addColumn(column);
        }

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            boolean hasMoreResultSets = stmt.execute(sql);
            while (hasMoreResultSets ||
                    stmt.getUpdateCount() != -1) { //if there are any more queries to be processed
                if (hasMoreResultSets) {
                    ResultSet rs = stmt.getResultSet();
                    if (rs.isBeforeFirst()) {
                        jasperReport.setDataSource(rs);
                        return jasperReport;
                    }
                }
                hasMoreResultSets = stmt.getMoreResults(); //true if it is a resultset
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jasperReport;
    }

    private String getFormattedSql(String formattedSql, ObsTemplateConfig reportConfig, String conceptNameInClause, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("conceptNameInClause", conceptNameInClause);
        sqlTemplate.add("conceptNameInClauseEscapeQuote", conceptNameInClause.replace("'", "\\'"));
        sqlTemplate.add("templateName",  reportConfig.getTemplateName());
        sqlTemplate.add("startDate",  startDate);
        sqlTemplate.add("endDate",  endDate);
        return sqlTemplate.render();
    }
}
