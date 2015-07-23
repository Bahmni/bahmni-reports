package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.MultipleCodedObsByCodedObsReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.MultipleCodedObsByObsReportTemplate;

public class MultipleCodedObsByCodedObsReport extends Report<MultipleCodedObsByCodedObsReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new MultipleCodedObsByObsReportTemplate();
    }
}
