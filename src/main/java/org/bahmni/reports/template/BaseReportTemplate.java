package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.ReportConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseReportTemplate {
    public abstract DataSource getDataSource();

    public JasperReportBuilder build(ReportConfig reportConfig, String startDate, String endDate) throws SQLException, DRException {
        Connection connection = getDataSource().getConnection();
        return buildReport(connection, reportConfig, startDate, endDate);
    }

    protected abstract JasperReportBuilder buildReport(Connection connection, ReportConfig reportConfig, String startDate, String endDate) throws SQLException, DRException;

}
