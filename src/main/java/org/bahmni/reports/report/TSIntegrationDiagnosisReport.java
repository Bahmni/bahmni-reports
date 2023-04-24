package org.bahmni.reports.report;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsConfiguration;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.TSIntegrationDiagnosisReportTemplate;
import org.bahmni.webclients.HttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class TSIntegrationDiagnosisReport extends Report<TSIntegrationDiagnosisReportConfig> {
    private static Logger logger = Logger.getLogger(TSIntegrationDiagnosisReport.class);
    private static final String TS_PROPERTIES_FILENAME = "./terminology-service-config.properties";
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        HttpClient httpClient = getHttpClient(bahmniReportsProperties);
        Properties tsProperties = getTSProperties();
        String tsEndpointTemplate = bahmniReportsProperties.getOpenmrsRootUrl() + "terminologyServices/searchTerminologyCodes?code={0}&size={1,number,#}&offset={2,number,#}&locale={3}";
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
        Path propertyFilePath = Paths.get(TS_PROPERTIES_FILENAME);
        if (Files.exists(propertyFilePath)) {
            Properties properties = new Properties();
            try {
                logger.info("Reading properties from: " + propertyFilePath);
                properties.load(Files.newInputStream(propertyFilePath));
                return properties;
            } catch (IOException e) {
                logger.error("Could not load terminology service properties from: " + propertyFilePath, e);
            }
        } else {
            logger.warn("No terminology service configuration defined at " + propertyFilePath);
        }
        return null;
    }

}
