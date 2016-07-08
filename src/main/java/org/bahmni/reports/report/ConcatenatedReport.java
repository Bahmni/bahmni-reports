package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ConcatenatedReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.ConcatenatedReportTemplate;

public class ConcatenatedReport extends Report<ConcatenatedReportConfig>{
    @Override
    public ConcatenatedReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new ConcatenatedReportTemplate(bahmniReportsProperties);
    }
}
