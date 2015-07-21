package org.bahmni.reports.template;

import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

@UsingDatasource("openelis")
public class ElisSqlReportTemplate extends SqlReportTemplate {
}
