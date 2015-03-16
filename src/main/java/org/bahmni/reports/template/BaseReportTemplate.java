package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface BaseReportTemplate<T extends Config> {
    JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<T> reportConfig,
                              String startDate, String endDate, List<AutoCloseable> resources)
            throws SQLException, DRException;
}
