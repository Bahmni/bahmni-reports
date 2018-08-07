package org.bahmni.reports.template;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.UsingDatasource;

@UsingDatasource("openmrs")
public class OpenmrsConcatenatedReportTemplate extends ConcatenatedReportTemplate {
    public OpenmrsConcatenatedReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        super(bahmniReportsProperties);
    }
}
