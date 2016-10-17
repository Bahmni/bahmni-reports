package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ProgramEnrollmentConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.ProgramEnrollmentTemplate;

public class ProgramEnrollmentReport extends Report<ProgramEnrollmentConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new ProgramEnrollmentTemplate();
    }
}
