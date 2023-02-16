package org.bahmni.reports.report;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.bahmni.reports.BahmniReportsConfiguration;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SnomedDiagnosisReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.SnomedDiagnosisReportTemplate;
import org.bahmni.webclients.HttpClient;

public class SnomedDiagnosisReport extends Report<SnomedDiagnosisReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        BahmniReportsConfiguration bahmniReportsConfiguration = new BahmniReportsConfiguration(bahmniReportsProperties);
        SSLConnectionSocketFactory allTrustSSLSocketFactory = bahmniReportsConfiguration.allTrustSSLSocketFactory();
        Registry<ConnectionSocketFactory> schemeRegistry = bahmniReportsConfiguration.schemeRegistry(allTrustSSLSocketFactory);
        HttpClient httpClient = bahmniReportsConfiguration.httpClient(schemeRegistry);
        return new SnomedDiagnosisReportTemplate(httpClient);
    }

}
