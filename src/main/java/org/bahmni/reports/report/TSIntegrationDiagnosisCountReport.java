package org.bahmni.reports.report;


import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisCountReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.TSIntegrationDiagnosisCountReportTemplate;
import org.bahmni.reports.util.PropertyUtil;
import org.bahmni.webclients.HttpClient;

import java.util.Properties;

public class TSIntegrationDiagnosisCountReport extends Report<TSIntegrationDiagnosisCountReportConfig> implements TSHttpClient {

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        HttpClient httpClient = getHttpClient(bahmniReportsProperties);
        Properties tsProperties = PropertyUtil.loadProperties(TS_PROPERTIES_FILENAME);
        String tsEndpointTemplate = bahmniReportsProperties.getOpenmrsRootUrl() + tsProperties.getProperty(TERMINOLOGY_SERVER_ENDPOINT_PROP);
        return new TSIntegrationDiagnosisCountReportTemplate(httpClient, tsProperties, tsEndpointTemplate);
    }
}
