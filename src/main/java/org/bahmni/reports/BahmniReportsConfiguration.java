package org.bahmni.reports;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.beans.PropertyVetoException;

@Import(BahmniReportsProperties.class)
@Configuration
public class BahmniReportsConfiguration {
    @Autowired
    private BahmniReportsProperties bahmniReportsProperties;

    @Bean
    public ComboPooledDataSource openmrsDataSource() throws PropertyVetoException {
        ComboPooledDataSource openmrsDataSource = new ComboPooledDataSource();
        openmrsDataSource.setJdbcUrl(bahmniReportsProperties.getOpenmrsUrl());
        openmrsDataSource.setUser(bahmniReportsProperties.getOpenmrsUser());
        openmrsDataSource.setPassword(bahmniReportsProperties.getOpenmrsPassword());
        openmrsDataSource.setDriverClass(com.mysql.jdbc.Driver.class.getName());
        openmrsDataSource.setAcquireIncrement(2);
        openmrsDataSource.setMinPoolSize(1);
        openmrsDataSource.setMaxPoolSize(15);
        return openmrsDataSource;
    }

    @Bean
    public ComboPooledDataSource openelisDataSource() throws PropertyVetoException {
        ComboPooledDataSource openelisDataSource = new ComboPooledDataSource();
        openelisDataSource.setJdbcUrl(bahmniReportsProperties.getOpenelisUrl());
        openelisDataSource.setUser(bahmniReportsProperties.getOpenelisUser());
        openelisDataSource.setPassword(bahmniReportsProperties.getOpenelisPassword());
        openelisDataSource.setDriverClass(org.postgresql.Driver.class.getName());
        openelisDataSource.setAcquireIncrement(2);
        openelisDataSource.setMinPoolSize(1);
        openelisDataSource.setMaxPoolSize(15);
        return openelisDataSource;
    }

}