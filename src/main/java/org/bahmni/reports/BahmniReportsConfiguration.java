package org.bahmni.reports;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.bahmni.reports.builder.ComboPooledDataSourceBuilder;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.beans.PropertyVetoException;

@Configuration
public class BahmniReportsConfiguration {

    @Autowired
    private BahmniReportsProperties bahmniReportsProperties;

    @Bean
    public HttpClient httpClient() {
        ConnectionDetails connectionDetails = new ConnectionDetails(bahmniReportsProperties.getOpenmrsRootUrl() + "/session",
                bahmniReportsProperties.getOpenmrsServiceUser(),
                bahmniReportsProperties.getOpenmrsServicePassword(), bahmniReportsProperties.getOpenmrsConnectionTimeout(),
                bahmniReportsProperties.getOpenmrsReplyTimeout());
        HttpClient httpClient = new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));
        return httpClient;
    }

    @Bean
    public ComboPooledDataSource openmrsDataSource() throws PropertyVetoException {
        ComboPooledDataSourceBuilder comboPooledDataSourceBuilder = new ComboPooledDataSourceBuilder();
        return comboPooledDataSourceBuilder.withUrl(bahmniReportsProperties.getOpenmrsUrl())
                .withUser(bahmniReportsProperties.getOpenmrsUser())
                .withPassword(bahmniReportsProperties.getOpenmrsPassword())
                .withDriver(com.mysql.jdbc.Driver.class).build();
    }

    @Bean
    public ComboPooledDataSource openelisDataSource() throws PropertyVetoException {
        ComboPooledDataSourceBuilder comboPooledDataSourceBuilder = new ComboPooledDataSourceBuilder();
        return comboPooledDataSourceBuilder.withUrl(bahmniReportsProperties.getOpenelisUrl())
                .withUser(bahmniReportsProperties.getOpenelisUser())
                .withPassword(bahmniReportsProperties.getOpenelisPassword())
                .withDriver(org.postgresql.Driver.class).build();
    }

}