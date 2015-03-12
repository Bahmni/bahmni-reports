package org.bahmni.reports.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class AllDatasources {

    @Autowired
    private DataSource openmrsDataSource;

    @Autowired
    private DataSource openelisDataSource;

    public DataSource dataSourceFor(Object object) {
        Class<?> annotatedClass = object.getClass();
        if (annotatedClass.isAnnotationPresent(UsingDatasource.class)) {
            UsingDatasource annotation = annotatedClass.getAnnotation(UsingDatasource.class);
            return dataSourceFor(annotation.value());
        }
        return null;
    }

    private DataSource dataSourceFor(String value) {
        switch (value) {
            case "openelis":
                return openelisDataSource;
            case "openmrs":
                return openmrsDataSource;
            default:
                throw new RuntimeException("No datasource found for " + value + ". Verify value of UsingDatasource annotation. ");
        }
    }

}
