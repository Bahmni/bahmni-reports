package org.bahmni.reports.template;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

public abstract class AbstractElisReportTemplate extends BaseReportTemplate {
    @Autowired
    private DataSource openelisDataSource;
    @Override
    public DataSource getDataSource() {
        return openelisDataSource;
    }
}
