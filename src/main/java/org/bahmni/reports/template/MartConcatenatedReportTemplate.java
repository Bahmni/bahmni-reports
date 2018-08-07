package org.bahmni.reports.template;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.UsingDatasource;

@UsingDatasource("bahmniMart")
public class MartConcatenatedReportTemplate extends ConcatenatedReportTemplate {
    public MartConcatenatedReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        super(bahmniReportsProperties);
    }
}
