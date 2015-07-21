package org.bahmni.reports.report;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SqlReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.ElisSqlReportTemplate;
import org.bahmni.reports.template.MRSSqlReportTemplate;

public class MRSSqlReport extends Report<SqlReportConfig> {

    private static final Logger logger = Logger.getLogger(MRSSqlReport.class);

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new MRSSqlReportTemplate();
    }
}
