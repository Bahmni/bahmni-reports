package org.bahmni.reports.template;

import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

@UsingDatasource("openmrs")
public class MRSSqlReportTemplate extends SqlReportTemplate {
}
