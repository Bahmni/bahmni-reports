package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SqlReportConfig;
import org.bahmni.reports.template.BahmniMartSqlReportTemplate;
import org.bahmni.reports.template.BaseReportTemplate;

public class BahmniMartSqlReport extends Report<SqlReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new BahmniMartSqlReportTemplate();
    }
}
