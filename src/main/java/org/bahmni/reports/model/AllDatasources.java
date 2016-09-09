package org.bahmni.reports.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class AllDatasources {

    @Autowired
    private DataSource openerpDataSource;

    @Autowired
    private DataSource openmrsDataSource;

    @Autowired
    private DataSource openelisDataSource;

    @Autowired
    private DataSource bahmniReportsDataSource;

    public Connection getConnectionFromDatasource(Object object) {
        Connection connection = null;
        try {
            connection = dataSourceFor(object).getConnection();
            if(connection.getAutoCommit()){
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private DataSource dataSourceFor(Object object) {
        Class<?> annotatedClass = object.getClass();
        if (annotatedClass.isAnnotationPresent(UsingDatasource.class)) {
            UsingDatasource annotation = annotatedClass.getAnnotation(UsingDatasource.class);
            DataSource dataSource = dataSourceFor(annotation.value());
            return dataSource;
        }
        return null;
    }

    public DataSource dataSourceFor(String value) {
        switch (value) {
            case "openelis":
                return openelisDataSource;
            case "openmrs":
                return openmrsDataSource;
            case "openerp":
                return openerpDataSource;
            case "bahmniReports":
                return bahmniReportsDataSource;
            default:
                throw new RuntimeException("No datasource found for " + value + ". Verify value of UsingDatasource annotation. ");
        }
    }

}
