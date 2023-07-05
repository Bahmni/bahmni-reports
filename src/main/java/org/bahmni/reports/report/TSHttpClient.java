package org.bahmni.reports.report;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.BahmniReportsConfiguration;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.webclients.HttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public interface TSHttpClient {
    Logger logger = LogManager.getLogger(TSHttpClient.class);
    final static String TS_PROPERTIES_FILENAME = "terminology-service-config.properties";

    default HttpClient getHttpClient(BahmniReportsProperties bahmniReportsProperties) {
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
