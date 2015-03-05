package org.bahmni.reports.template;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

public abstract class AbstractMRSReportTemplate extends BaseReportTemplate {
    @Autowired
    private DataSource openmrsDataSource;

    @Override
    public DataSource getDataSource() {
        return openmrsDataSource;
    }
}
