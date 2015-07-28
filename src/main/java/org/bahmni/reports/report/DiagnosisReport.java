package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.DiagnosisCountByAgeGroup;
import org.bahmni.reports.template.DiagnosisCountWithoutAgeGroup;
import org.bahmni.reports.template.DiagnosisSummaryTemplate;

import static org.springframework.util.StringUtils.isEmpty;

public class DiagnosisReport extends Report<DiagnosisReportConfig> {

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        if (!isEmpty(getConfig().getConcept())) {
            return new DiagnosisSummaryTemplate();
        }

        if (!isEmpty(this.getConfig().getAgeGroupName())) {
            return new DiagnosisCountByAgeGroup();
        }

        return new DiagnosisCountWithoutAgeGroup();
    }
}
