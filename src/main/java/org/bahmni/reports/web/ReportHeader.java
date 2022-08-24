package org.bahmni.reports.web;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.template.Templates;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;

public class ReportHeader {

    private String reportTimeZone;

    public ReportHeader() {
        this.reportTimeZone = ZoneId.systemDefault().getId();
    }

    public ReportHeader(String reportTimeZone) {
        this.reportTimeZone = reportTimeZone;
    }

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
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("yyyy-MM-dd hh:mm:ss")
                .toFormatter();
        if (StringUtils.isBlank(reportTimeZone)) {
            reportTimeZone = ZoneId.systemDefault().getId();
        }
        ZoneId rZone = ZoneId.of(reportTimeZone);
        ZonedDateTime nowLocalTime = ZonedDateTime.now(rZone);
        String dateString = formatter.format(nowLocalTime);
        headerList.add(cmp.text("Report Generated On: " + dateString)
                .setStyle(Templates.bold12CenteredStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER))
                .newRow();
    }

}
