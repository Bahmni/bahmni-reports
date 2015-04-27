package org.bahmni.reports.template;


import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ConceptDetails;
import org.bahmni.reports.model.ObsTemplateConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

@Component(value = "obsTemplate")
@UsingDatasource("openmrs")
public class ObsTemplate implements BaseReportTemplate<ObsTemplateConfig> {

    @Autowired
    private BahmniReportsProperties bahmniReportsProperties;

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsTemplateConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException, URISyntaxException, IOException {
        String templateName = reportConfig.getConfig().getTemplateName();


        ConnectionDetails connectionDetails = new ConnectionDetails(bahmniReportsProperties.getOpenmrsRootUrl() + "/session", bahmniReportsProperties.getOpenmrsServiceUser(),
                bahmniReportsProperties.getOpenmrsServicePassword(), bahmniReportsProperties.getOpenmrsConnectionTimeout(), bahmniReportsProperties.getOpenmrsReplyTimeout());
        HttpClient httpClient = new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));

        String url = bahmniReportsProperties.getOpenmrsRootUrl() +"/reference-data/leafConcepts?conceptName=" + URLEncoder.encode(templateName, "UTF-8");
        String response = httpClient.get(new URI(url));

        ObjectMapper objectMapper = new ObjectMapper();
        List<ConceptDetails> conceptDetails = objectMapper.readValue(response, new TypeReference<List<ConceptDetails>>() {});
        List<String> conceptNames = new ArrayList<>();
        for (ConceptDetails conceptDetail : conceptDetails) {
           conceptNames.add( "'" + conceptDetail.getFullName() +  "'");
        }

        String conceptNameInClause = StringUtils.join(conceptNames, ",");
        String sql = String.format(getFileContent("sql/obsTemplate.sql"), conceptNameInClause, startDate, endDate, conceptNameInClause.replace("'","\\'"));

        TextColumnBuilder<String> patientColumn = col.column("Patient", "identifier", type.stringType());
        TextColumnBuilder<String> providerColumn = col.column("User", "provider_name", type.stringType());
        TextColumnBuilder<String> encounterColumn = col.column("Encounter DateTime", "encounter_datetime", type.stringType());

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .columns(patientColumn, providerColumn, encounterColumn)
                .pageFooter(Templates.footerComponent);
        for(ConceptDetails concept: conceptDetails){
            TextColumnBuilder<String> column = col.column(concept.getName(), concept.getFullName(), type.stringType());
            jasperReport.addColumn(column);
        }

        Statement stmt = connection.createStatement();
        boolean hasMoreResultSets= stmt.execute(sql);
        while ( hasMoreResultSets ||
                stmt.getUpdateCount() != -1 ) { //if there are any more queries to be processed
            if ( hasMoreResultSets ) {
                ResultSet rs = stmt.getResultSet();
                if(rs.isBeforeFirst()) {
                    jasperReport.setDataSource(rs);
                    return jasperReport;
                }
            }
            hasMoreResultSets = stmt.getMoreResults(); //true if it is a resultset
        }
        return  jasperReport;
    }
}
