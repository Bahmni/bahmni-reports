package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.DateConceptValuesConfig;
import org.bahmni.reports.model.PatientsWithLabtestResultsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.DateConceptValuesPatientsList;

public class DateConceptValuesPatientsListReport extends Report<DateConceptValuesConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new DateConceptValuesPatientsList();
    }
}
