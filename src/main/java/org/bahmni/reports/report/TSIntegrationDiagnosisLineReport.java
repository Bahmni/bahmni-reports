package org.bahmni.reports.report;


import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisLineReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.TSIntegrationDiagnosisLineReportTemplate;
import org.bahmni.webclients.HttpClient;


import java.util.Properties;

public class TSIntegrationDiagnosisLineReport extends Report<TSIntegrationDiagnosisLineReportConfig> implements TSHttpClient {


    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        HttpClient httpClient = getHttpClient(bahmniReportsProperties);
        Properties tsProperties = getTSProperties();
        String tsEndpointTemplate = bahmniReportsProperties.getOpenmrsRootUrl() + tsProperties.getProperty("terminologyServer.endpoint");
        return new TSIntegrationDiagnosisLineReportTemplate(httpClient, tsProperties, tsEndpointTemplate);
    }

}
