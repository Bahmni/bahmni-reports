package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.AggregationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.AggregationReportTemplate;

public class AggregationReport extends Report<AggregationReportConfig>{
    @Override
    public AggregationReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new AggregationReportTemplate(bahmniReportsProperties);
    }
}
