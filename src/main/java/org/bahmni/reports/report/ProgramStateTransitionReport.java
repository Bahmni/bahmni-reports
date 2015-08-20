package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ProgramStateTransitionConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.ProgramStateTransitionTemplate;

public class ProgramStateTransitionReport extends Report<ProgramStateTransitionConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new ProgramStateTransitionTemplate();
    }
}