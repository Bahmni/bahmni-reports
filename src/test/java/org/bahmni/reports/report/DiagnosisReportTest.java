package org.bahmni.reports.report;

import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.report.DiagnosisReport;
import org.bahmni.reports.template.DiagnosisCountByAgeGroup;
import org.bahmni.reports.template.DiagnosisCountWithoutAgeGroup;
import org.bahmni.reports.template.DiagnosisSummaryTemplate;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DiagnosisReportTest {

    @Test
    public void shouldPickupDiagnosisSummaryReportWhenConceptProvided() {
        DiagnosisReportConfig diagnosisReportConfig = new DiagnosisReportConfig();

        DiagnosisReport diagnosisReport = new DiagnosisReport();
        diagnosisReport.setConfig(diagnosisReportConfig);
        assertTrue(diagnosisReport.getTemplate(null).getClass().isAssignableFrom(DiagnosisCountWithoutAgeGroup.class));

        diagnosisReportConfig.setConcept("some_concept");
        assertTrue(diagnosisReport.getTemplate(null).getClass().isAssignableFrom(DiagnosisSummaryTemplate.class));

        diagnosisReportConfig.setAgeGroupName("dummy_age_group");
        assertTrue(diagnosisReport.getTemplate(null).getClass().isAssignableFrom(DiagnosisSummaryTemplate.class));
    }

    @Test
    public void shouldPickupDiagnosisReportWithAgegroupIfAgegroupSpecified() {
        DiagnosisReportConfig diagnosisReportConfig = new DiagnosisReportConfig();
        diagnosisReportConfig.setAgeGroupName("dummy_age_group");

        DiagnosisReport diagnosisReport = new DiagnosisReport();
        diagnosisReport.setConfig(diagnosisReportConfig);
        assertTrue(diagnosisReport.getTemplate(null).getClass().isAssignableFrom(DiagnosisCountByAgeGroup.class));
    }
}
