package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.VisitReportConfig;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.VisitReportTemplate;

public class VisitReport extends Report<VisitReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new VisitReportTemplate();
    }
}
