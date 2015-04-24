package org.bahmni.reports.template;


import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.ConceptDetails;
import org.bahmni.reports.model.ObsTemplateConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "obsTemplate")
@UsingDatasource("openmrs")
public class ObsTemplate implements BaseReportTemplate<ObsTemplateConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsTemplateConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException, URISyntaxException, IOException {
        String templateName = reportConfig.getConfig().getTemplateName();

        String url = "http://localhost:8080/openmrs/ws/rest/v1/reference-data/leafConcepts?conceptName=" + templateName;
        ConnectionDetails connectionDetails = new ConnectionDetails("http://localhost:8080/openmrs/ws/rest/v1/session", "admin", "test", 1000, 100);
        HttpClient httpClient = new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));
        String response = httpClient.get(new URI(url));

        ObjectMapper objectMapper = new ObjectMapper();
        List<ConceptDetails> conceptDetails = objectMapper.readValue(response, new TypeReference<List<ConceptDetails>>() {});
        List<String> conceptNames = new ArrayList<>();
        for (ConceptDetails conceptDetail : conceptDetails) {
           conceptNames.add( "'" + conceptDetail.getName() +  "'");
        }

        String conceptNameInClause = StringUtils.join(conceptNames, ",");
        String sql = String.format(getFileContent("sql/obsTemplate.sql"), conceptNameInClause, conceptNameInClause);

        System.out.println("===========================");
        System.out.println(sql);
        System.out.println("===========================");
        TextColumnBuilder<String> patientColumn = col.column("Patient", "identifier", type.stringType());
        TextColumnBuilder<String> providerColumn = col.column("User", "provider_name", type.stringType());
        TextColumnBuilder<String> encounterColumn = col.column("Encounter DateTime", "encounter_datetime", type.stringType());

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .columns(patientColumn, providerColumn, encounterColumn)
                .pageFooter(Templates.footerComponent)
                .setDataSource(sql, connection);
        for(ConceptDetails concept: conceptDetails){
            TextColumnBuilder<String> column = col.column(concept.getName(), concept.getName(), type.stringType());
            jasperReport.addColumn(column);
        }
        return jasperReport;

    }
}
