package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.VisitAggregateCountConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.VisitAggregateCountReportTemplate;

public class VisitAggregateCountReport extends Report<VisitAggregateCountConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new VisitAggregateCountReportTemplate();
    }
}
