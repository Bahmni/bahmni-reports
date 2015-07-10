package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class BaseReportTemplate<T extends Config> {
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<T> reportConfig,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        jasperReport.setPageFormat(pageType, PageOrientation.LANDSCAPE);
        jasperReport.pageFooter(Templates.footerComponent);
        jasperReport.setReportName(reportConfig.getName());
        jasperReport.setTemplate(Templates.reportTemplate);
        return jasperReport;
    }
}
