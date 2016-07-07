package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.report.BahmniReportBuilder;

import java.sql.Connection;
import java.util.List;

public abstract class BaseReportTemplate<T extends Config> {
    public abstract BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<T> report,
                                              String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws Exception;
}