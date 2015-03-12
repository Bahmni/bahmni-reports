package org.bahmni.reports.template;

import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

@Component(value = "MRSGeneric")
@UsingDatasource("openmrs")
public class MRSSqlReportTemplate extends SqlReportTemplate {
}
