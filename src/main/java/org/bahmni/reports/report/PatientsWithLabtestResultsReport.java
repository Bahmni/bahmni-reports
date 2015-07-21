package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.PatientsWithLabtestResultsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.PatientsWithLabtestResults;

public class PatientsWithLabtestResultsReport extends Report<PatientsWithLabtestResultsConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new PatientsWithLabtestResults();
    }
}
