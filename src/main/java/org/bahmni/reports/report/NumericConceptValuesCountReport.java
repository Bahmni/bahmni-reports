package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.NumericConceptValuesConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.NumericConceptValuesReportTemplate;

public class NumericConceptValuesCountReport extends Report<NumericConceptValuesConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new NumericConceptValuesReportTemplate();
    }
}
