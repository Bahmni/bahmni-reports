package org.bahmni.reports.report;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.BahmniReportsConfiguration;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.TSIntegrationDiagnosisReportTemplate;
import org.bahmni.webclients.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TSIntegrationDiagnosisReport extends Report<TSIntegrationDiagnosisReportConfig> {
    private static final Logger logger = LogManager.getLogger(TSIntegrationDiagnosisReport.class);
    private static final String TS_PROPERTIES_FILENAME = "terminology-service-config.properties";

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        HttpClient httpClient = getHttpClient(bahmniReportsProperties);
        Properties tsProperties = getTSProperties();
        String tsEndpointTemplate = bahmniReportsProperties.getOpenmrsRootUrl() + tsProperties.getProperty("ts.endpoint");
        return new TSIntegrationDiagnosisReportTemplate(httpClient, tsProperties, tsEndpointTemplate);
    }

    private static HttpClient getHttpClient(BahmniReportsProperties bahmniReportsProperties) {
        BahmniReportsConfiguration bahmniReportsConfiguration = new BahmniReportsConfiguration(bahmniReportsProperties);
        SSLConnectionSocketFactory allTrustSSLSocketFactory = bahmniReportsConfiguration.allTrustSSLSocketFactory();
        Registry<ConnectionSocketFactory> schemeRegistry = bahmniReportsConfiguration.schemeRegistry(allTrustSSLSocketFactory);
        HttpClient httpClient = bahmniReportsConfiguration.httpClient(schemeRegistry);
        return httpClient;
    }

    private Properties getTSProperties() {
        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(TS_PROPERTIES_FILENAME);
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            logger.error("Could not load terminology service properties from: " + TS_PROPERTIES_FILENAME, e);
            throw new RuntimeException();
        }
    }

}
