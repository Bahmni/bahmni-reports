package org.bahmni.reports;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Logger;
import org.apache.poi.hpsf.IllegalPropertySetDataException;
import org.bahmni.reports.builder.ComboPooledDataSourceBuilder;
import org.bahmni.reports.util.GlobalPropertyDao;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.openmrs.OpenMRSLoginAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.SQLException;

@Configuration
public class BahmniReportsConfiguration {
    private static final Logger logger = Logger.getLogger(BahmniReportsConfiguration.class);

    @Autowired
    private BahmniReportsProperties bahmniReportsProperties;

    @Bean
    public HttpClient httpClient(DataSource openmrsDataSource) throws SQLException {
        String reportUserPassword = GlobalPropertyDao.getReportUserPassword(openmrsDataSource.getConnection());
        if (reportUserPassword == null) {
            logger.error("Password is not set for user: \"" + bahmniReportsProperties.getOpenmrsServiceUser() + "\" in global properties.");
            throw new IllegalPropertySetDataException("Password is not set for user: \"" + bahmniReportsProperties.getOpenmrsServiceUser() + "\" in global properties.");
        }
        ConnectionDetails connectionDetails = new ConnectionDetails(bahmniReportsProperties.getOpenmrsRootUrl() + "/session",
                bahmniReportsProperties.getOpenmrsServiceUser(),
                reportUserPassword, bahmniReportsProperties.getOpenmrsConnectionTimeout(),
                bahmniReportsProperties.getOpenmrsReplyTimeout());
        return new HttpClient(connectionDetails, new OpenMRSLoginAuthenticator(connectionDetails));
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