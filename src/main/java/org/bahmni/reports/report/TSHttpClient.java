package org.bahmni.reports.report;

import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.BahmniReportsConfiguration;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.webclients.HttpClient;

public interface TSHttpClient {
    Logger logger = LogManager.getLogger(TSHttpClient.class);
    final String TS_PROPERTIES_FILENAME = "terminology-service-config.properties";
    final String TERMINOLOGY_SERVER_ENDPOINT_PROP = "terminologyServer.endpoint";

    default HttpClient getHttpClient(BahmniReportsProperties bahmniReportsProperties) {
        BahmniReportsConfiguration bahmniReportsConfiguration = new BahmniReportsConfiguration(bahmniReportsProperties);
        SSLConnectionSocketFactory allTrustSSLSocketFactory = bahmniReportsConfiguration.allTrustSSLSocketFactory();
        Registry<ConnectionSocketFactory> schemeRegistry = bahmniReportsConfiguration.schemeRegistry(allTrustSSLSocketFactory);
        HttpClient httpClient = bahmniReportsConfiguration.httpClient(schemeRegistry);
        return httpClient;
    }
}
