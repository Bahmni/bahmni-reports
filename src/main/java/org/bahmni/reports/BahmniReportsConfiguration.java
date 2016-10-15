package org.bahmni.reports;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.bahmni.reports.builder.ComboPooledDataSourceBuilder;
import org.bahmni.webclients.AllTrustedSSLSocketFactory;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.beans.PropertyVetoException;

@Configuration
public class BahmniReportsConfiguration {


    private static int IDLE_CONNECTION_TEST_TIME = 300; //in seconds

    private BahmniReportsProperties bahmniReportsProperties;

    @Autowired
    public BahmniReportsConfiguration(BahmniReportsProperties bahmniReportsProperties){
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Bean
    public HttpClient httpClient(SchemeRegistry schemeRegistry) {
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setDefaultMaxPerRoute(10);
        ConnectionDetails connectionDetails = new ConnectionDetails(bahmniReportsProperties.getOpenmrsRootUrl() + "/session",
                bahmniReportsProperties.getOpenmrsServiceUser(),
                bahmniReportsProperties.getOpenmrsServicePassword(), bahmniReportsProperties.getOpenmrsConnectionTimeout(),
                bahmniReportsProperties.getOpenmrsReplyTimeout(), connectionManager);
        return new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));
    }


    @Bean
    public SchemeRegistry schemeRegistry(SSLSocketFactory allTrustSSLSocketFactory){

        if(!"true".equals(bahmniReportsProperties.getTrustSSLConnection())){
            return SchemeRegistryFactory.createDefault();
        }

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        registry.register(
                new Scheme("https", 443, allTrustSSLSocketFactory));

        return registry;
    }

    @Bean
    public SSLSocketFactory allTrustSSLSocketFactory(){
        return new AllTrustedSSLSocketFactory().getSSLSocketFactory();
    }

    @Bean
    public ComboPooledDataSource openmrsDataSource() throws PropertyVetoException {
        ComboPooledDataSourceBuilder comboPooledDataSourceBuilder = new ComboPooledDataSourceBuilder();
        ComboPooledDataSource dataSource = comboPooledDataSourceBuilder.withUrl(bahmniReportsProperties.getOpenmrsUrl())
                .withUser(bahmniReportsProperties.getOpenmrsUser())
                .withPassword(bahmniReportsProperties.getOpenmrsPassword())
                .withDriver(com.mysql.jdbc.Driver.class).build();

        dataSource.setIdleConnectionTestPeriod(IDLE_CONNECTION_TEST_TIME);
        dataSource.setPreferredTestQuery("SELECT 1;");
        return dataSource;
    }

    @Bean
    public ComboPooledDataSource openelisDataSource() throws PropertyVetoException {
        ComboPooledDataSourceBuilder comboPooledDataSourceBuilder = new ComboPooledDataSourceBuilder();
        ComboPooledDataSource dataSource = comboPooledDataSourceBuilder.withUrl(bahmniReportsProperties.getOpenelisUrl())
                .withUser(bahmniReportsProperties.getOpenelisUser())
                .withPassword(bahmniReportsProperties.getOpenelisPassword())
                .withDriver(Driver.class).build();

        dataSource.setIdleConnectionTestPeriod(IDLE_CONNECTION_TEST_TIME);
        dataSource.setPreferredTestQuery("SELECT 1;");
        return dataSource;
    }

    @Bean
    public ComboPooledDataSource bahmniReportsDataSource() throws PropertyVetoException {
        ComboPooledDataSourceBuilder comboPooledDataSourceBuilder = new ComboPooledDataSourceBuilder();
        ComboPooledDataSource dataSource = comboPooledDataSourceBuilder.withUrl(bahmniReportsProperties.getBahmniReportsDbUrl())
                .withUser(bahmniReportsProperties.getReportsUser())
                .withPassword(bahmniReportsProperties.getReportsPassword())
                .withDriver(com.mysql.jdbc.Driver.class).build();

        dataSource.setIdleConnectionTestPeriod(IDLE_CONNECTION_TEST_TIME);
        dataSource.setPreferredTestQuery("SELECT 1;");
        return dataSource;
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("utf-8");
        commonsMultipartResolver.setMaxUploadSize(50000000);
        return commonsMultipartResolver;
    }

    @Bean
    public ComboPooledDataSource openerpDataSource() throws PropertyVetoException {
        ComboPooledDataSourceBuilder comboPooledDataSourceBuilder = new ComboPooledDataSourceBuilder();
        ComboPooledDataSource dataSource = comboPooledDataSourceBuilder.withUrl(bahmniReportsProperties.getOpenERPUrl())
                .withUser(bahmniReportsProperties.getOpenERPUser())
                .withPassword(bahmniReportsProperties.getOpenERPPassword())
                .withDriver(Driver.class).build();

        dataSource.setIdleConnectionTestPeriod(IDLE_CONNECTION_TEST_TIME);
        dataSource.setPreferredTestQuery("SELECT 1;");
        return dataSource;
    }

}
