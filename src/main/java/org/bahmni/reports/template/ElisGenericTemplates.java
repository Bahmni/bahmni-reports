package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.ReportConfig;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Component(value = "ElisGeneric")
public class ElisGenericTemplates extends AbstractElisReportTemplate {
    @Override
    protected JasperReportBuilder buildReport(Connection connection, ReportConfig reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        return buildGenericReport(connection, reportConfig, startDate, endDate, resources);
    }
}
