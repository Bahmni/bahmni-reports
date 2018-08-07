package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ConcatenatedReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.ConcatenatedReportTemplate;
import org.bahmni.reports.template.OpenmrsConcatenatedReportTemplate;

public class OpenmrsConcatenatedReport extends Report<ConcatenatedReportConfig>{
    @Override
    public ConcatenatedReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new OpenmrsConcatenatedReportTemplate(bahmniReportsProperties);
    }
}
