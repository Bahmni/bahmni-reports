package org.bahmni.reports.api.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.json.simple.JSONObject;

import java.sql.SQLException;

public interface BaseReportTemplate {
    public JasperReportBuilder build(JSONObject jsonObject, String startDate, String endDate) throws SQLException;
}
