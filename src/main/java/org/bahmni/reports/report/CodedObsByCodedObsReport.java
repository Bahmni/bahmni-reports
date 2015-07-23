package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.CodedObsByCodedObsReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.CodedObsByObsReportTemplate;

public class CodedObsByCodedObsReport extends Report<CodedObsByCodedObsReportConfig> {

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new CodedObsByObsReportTemplate();
    }
}
