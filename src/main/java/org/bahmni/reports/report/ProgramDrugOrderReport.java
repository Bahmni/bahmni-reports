package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ProgramDrugOrderTemplateConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.ProgramDrugOrderTemplate;

public class ProgramDrugOrderReport extends Report<ProgramDrugOrderTemplateConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new ProgramDrugOrderTemplate();
    }
}

