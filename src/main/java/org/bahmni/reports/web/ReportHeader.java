package org.bahmni.reports.web;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.bahmni.reports.template.Templates;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;

public class ReportHeader {

    public JasperReportBuilder add(JasperReportBuilder jasperReportBuilder, String reportName, String startDate, String endDate) {
        jasperReportBuilder.title(cmp.horizontalList()
                        .add(cmp.text(reportName)
                                .setStyle(Templates.bold18CenteredStyle)
                                .setHorizontalAlignment(HorizontalAlignment.CENTER))
                        .newRow()
                        .add(cmp.verticalGap(5))
                        .add(cmp.text("From " + startDate + " to " + endDate)
                                .setStyle(Templates.bold12CenteredStyle)
                                .setHorizontalAlignment(HorizontalAlignment.CENTER))
                        .newRow()
                        .add(cmp.line())
                        .add(cmp.verticalGap(10))
        );
        return jasperReportBuilder;
    }
}
