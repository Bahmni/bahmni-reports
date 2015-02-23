package org.bahmni.reports.api;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;

import java.sql.SQLException;

public interface Report {
    public JasperReportBuilder run() throws SQLException;
}
