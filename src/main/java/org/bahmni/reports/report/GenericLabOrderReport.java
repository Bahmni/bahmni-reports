package org.bahmni.reports.report;


import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.GenericLabOrderReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.GenericLabOrderReportTemplate;

public class GenericLabOrderReport extends Report<GenericLabOrderReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new GenericLabOrderReportTemplate(bahmniReportsProperties);
    }
}
