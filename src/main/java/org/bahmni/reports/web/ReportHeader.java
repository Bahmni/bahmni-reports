package org.bahmni.reports.web;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.bahmni.reports.template.Templates;

import java.text.SimpleDateFormat;
import java.util.Date;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;

public class ReportHeader {

    public JasperReportBuilder add(JasperReportBuilder jasperReportBuilder, String reportName, String startDate, String endDate) {
        HorizontalListBuilder headerList = cmp.horizontalList();

        addTitle(reportName, headerList);

        addDatesSubHeader(startDate, endDate, headerList);

        addReportGeneratedDateSubHeader(headerList);

        addVerticalGap(headerList);

        jasperReportBuilder.addTitle(headerList);

        return jasperReportBuilder;
    }

    private void addVerticalGap(HorizontalListBuilder headerList) {
        headerList.add(cmp.line())
                .add(cmp.verticalGap(10));
    }

    private void addTitle(String reportName, HorizontalListBuilder headerList) {
        headerList.add(cmp.text(reportName)
                .setStyle(Templates.bold18CenteredStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER))
                .newRow()
                .add(cmp.verticalGap(5));
    }

    private void addDatesSubHeader(String startDate, String endDate, HorizontalListBuilder headerList) {
        if (startDate.equalsIgnoreCase("null") || endDate.equalsIgnoreCase("null")) return;

        headerList.add(cmp.text("From " + startDate + " to " + endDate)
                .setStyle(Templates.bold12CenteredStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER))
                .newRow();
    }

    private void addReportGeneratedDateSubHeader(HorizontalListBuilder headerList) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateString = simpleDateFormat.format(new Date());
        headerList.add(cmp.text("Report Generated On: " + dateString)
                .setStyle(Templates.bold12CenteredStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER))
                .newRow();
    }
}
