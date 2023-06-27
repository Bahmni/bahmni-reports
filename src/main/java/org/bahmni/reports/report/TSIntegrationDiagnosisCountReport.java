package org.bahmni.reports.report;


import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisCountReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.TSIntegrationDiagnosisCountReportTemplate;
import org.bahmni.webclients.HttpClient;

import java.util.Properties;

public class TSIntegrationDiagnosisCountReport extends Report<TSIntegrationDiagnosisCountReportConfig> implements TSHttpClient {


    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        HttpClient httpClient = getHttpClient(bahmniReportsProperties);
        Properties tsProperties = getTSProperties();
        String tsEndpointTemplate = bahmniReportsProperties.getOpenmrsRootUrl() + tsProperties.getProperty("ts.endpoint");
        return new TSIntegrationDiagnosisCountReportTemplate(httpClient, tsProperties, tsEndpointTemplate);
    }
}
