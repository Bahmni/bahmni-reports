package org.bahmni.reports.report;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.BahmniReportsConfiguration;
import org.bahmni.reports.BahmniReportsProperties;
<<<<<<< HEAD:src/main/java/org/bahmni/reports/report/TSHttpClient.java
=======
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisCountReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.TSIntegrationDiagnosisCountReportTemplate;
>>>>>>> e1ab372 (BS 54 | renamed existing count report files name to show the intent):src/main/java/org/bahmni/reports/report/TSIntegrationDiagnosisCountReport.java
import org.bahmni.webclients.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

<<<<<<< HEAD:src/main/java/org/bahmni/reports/report/TSHttpClient.java
public interface TSHttpClient {
    Logger logger = LogManager.getLogger(TSHttpClient.class);
    final static String TS_PROPERTIES_FILENAME = "terminology-service-config.properties";

    default HttpClient getHttpClient(BahmniReportsProperties bahmniReportsProperties) {
=======
public class TSIntegrationDiagnosisCountReport extends Report<TSIntegrationDiagnosisCountReportConfig> {
    private static final Logger logger = LogManager.getLogger(TSIntegrationDiagnosisCountReport.class);
    private static final String TS_PROPERTIES_FILENAME = "terminology-service-config.properties";

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        HttpClient httpClient = getHttpClient(bahmniReportsProperties);
        Properties tsProperties = getTSProperties();
        String tsEndpointTemplate = bahmniReportsProperties.getOpenmrsRootUrl() + tsProperties.getProperty("ts.endpoint");
        return new TSIntegrationDiagnosisCountReportTemplate(httpClient, tsProperties, tsEndpointTemplate);
    }

    private static HttpClient getHttpClient(BahmniReportsProperties bahmniReportsProperties) {
>>>>>>> e1ab372 (BS 54 | renamed existing count report files name to show the intent):src/main/java/org/bahmni/reports/report/TSIntegrationDiagnosisCountReport.java
        BahmniReportsConfiguration bahmniReportsConfiguration = new BahmniReportsConfiguration(bahmniReportsProperties);
        SSLConnectionSocketFactory allTrustSSLSocketFactory = bahmniReportsConfiguration.allTrustSSLSocketFactory();
        Registry<ConnectionSocketFactory> schemeRegistry = bahmniReportsConfiguration.schemeRegistry(allTrustSSLSocketFactory);
        HttpClient httpClient = bahmniReportsConfiguration.httpClient(schemeRegistry);
        return httpClient;
    }

    default Properties getTSProperties() {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(TS_PROPERTIES_FILENAME)) {
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            logger.error("Could not load terminology service properties from: " + TS_PROPERTIES_FILENAME, e);
            throw new RuntimeException();
        }
    }
}
