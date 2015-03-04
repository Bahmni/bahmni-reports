package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.bahmni.reports.model.ReportConfig;

import java.sql.SQLException;

public interface BaseReportTemplate {
    
    public JasperReportBuilder build(ReportConfig reportConfig, String startDate, String endDate) throws SQLException;
    
}
