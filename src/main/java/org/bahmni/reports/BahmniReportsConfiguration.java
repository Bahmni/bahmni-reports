package org.bahmni.reports;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.bahmni.reports.builder.ComboPooledDataSourceBuilder;
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

    @Autowired
    private BahmniReportsProperties bahmniReportsProperties;
    private static int IDLE_CONNECTION_TEST_TIME = 300; //in seconds

    @Bean
    public HttpClient httpClient() {
        ConnectionDetails connectionDetails = new ConnectionDetails(bahmniReportsProperties.getOpenmrsRootUrl() + "/session",
                bahmniReportsProperties.getOpenmrsServiceUser(),
                bahmniReportsProperties.getOpenmrsServicePassword(), bahmniReportsProperties.getOpenmrsConnectionTimeout(),
                bahmniReportsProperties.getOpenmrsReplyTimeout());
        return new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));
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