package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.Templates;

public class CommonComponents {
    public static JasperReportBuilder addTo(JasperReportBuilder jasperReport, Report report, PageType pageType) {
        return jasperReport
                .setPageFormat(pageType, PageOrientation.LANDSCAPE)
                .setReportName(report.getName())
                .setTemplate(Templates.reportTemplate);
    }
}
