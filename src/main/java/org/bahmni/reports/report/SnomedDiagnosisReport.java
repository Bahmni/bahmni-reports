package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ObsCountConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SnomedDiagnosisReportConfig;
import org.bahmni.reports.template.*;
import org.bahmni.reports.util.ConceptDataTypeException;
import org.bahmni.reports.util.ConceptDataTypes;
import org.bahmni.reports.util.ConceptUtil;

import java.util.List;

public class SnomedDiagnosisReport extends Report<SnomedDiagnosisReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new SnomedDiagnosisReportTemplate();
    }
}
