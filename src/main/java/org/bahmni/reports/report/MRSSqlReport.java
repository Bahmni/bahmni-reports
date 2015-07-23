package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SqlReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.MRSSqlReportTemplate;

public class MRSSqlReport extends Report<SqlReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new MRSSqlReportTemplate();
    }
}
