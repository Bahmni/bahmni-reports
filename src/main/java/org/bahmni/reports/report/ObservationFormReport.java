package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.GenericObservationFormReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.ObservationFormReportTemplate;

public class ObservationFormReport extends Report<GenericObservationFormReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new ObservationFormReportTemplate(bahmniReportsProperties);
    }
}
