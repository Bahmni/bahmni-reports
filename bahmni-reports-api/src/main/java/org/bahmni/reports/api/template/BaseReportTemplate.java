package org.bahmni.reports.api.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.bahmni.reports.api.model.ReportConfig;

import java.sql.SQLException;

public interface BaseReportTemplate {
    public JasperReportBuilder build(ReportConfig reportConfig) throws SQLException;
}
