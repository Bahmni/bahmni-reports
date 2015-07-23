package org.bahmni.reports.report;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.DiagnosisCountByAgeGroup;
import org.bahmni.reports.template.DiagnosisCountWithoutAgeGroup;

public class DiagnosisReport extends Report<DiagnosisReportConfig> {

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        if (StringUtils.isNotBlank(this.getConfig().getAgeGroupName())){
            return new DiagnosisCountByAgeGroup();
        }
        return new DiagnosisCountWithoutAgeGroup();
    }
}
