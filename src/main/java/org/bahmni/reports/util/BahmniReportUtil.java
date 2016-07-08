package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.web.ReportHeader;
import org.bahmni.webclients.HttpClient;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.report;

public class BahmniReportUtil {

    public static BahmniReportBuilder build(Report report, HttpClient httpClient, Connection connection,
                                            String startDate, String endDate,
                                            List<AutoCloseable> resources, PageType pageType,
                                            BahmniReportsProperties bahmniReportsProperties) throws Exception {
        report.setHttpClient(httpClient);
        BaseReportTemplate reportTemplate = report.getTemplate(bahmniReportsProperties);
        JasperReportBuilder subReportBuilder = report();
        subReportBuilder = new ReportHeader().add(subReportBuilder, report.getName(), startDate, endDate);
        return reportTemplate.build(connection, subReportBuilder, report, startDate, endDate, resources, pageType);
    }
}
