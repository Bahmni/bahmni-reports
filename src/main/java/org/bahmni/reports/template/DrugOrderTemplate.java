package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.util.CommonComponents;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.columnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class DrugOrderTemplate extends BaseReportTemplate<Config> {

    private String getFormattedSql(String formattedSql, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report report, String startDate, String endDate, List resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        TextColumnBuilder<String> drugName = col.column("Drug Name", "drugName", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> dose = col.column("Dose", "dose", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> unit = col.column("Unit", "unit", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> frequency = col.column("Frequency", "frequency", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> duration = col.column("Duration", "duration", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> route = col.column("Route", "route", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<Date> startdate = col.column("Start Date", "startDate", type.dateType())
                .setStyle(columnStyle);

        TextColumnBuilder<Date> stopDate = col.column("Stop Date", "stopDate", type.dateType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> quantity = col.column("Quantity", "quantity", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> patientId = col.column("Patient ID", "patientId", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> patientName = col.column("Patient Name", "patientName", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> patientGender = col.column("Gender", "gender", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> patientAge = col.column("Age", "age", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> user = col.column("User", "user", type.stringType())
                .setStyle(columnStyle);

        String sql = getFileContent("sql/drugOrderReport.sql");

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setShowColumnTitle(true)
                .columns(patientId,patientName,patientGender,patientAge,user,drugName,dose, unit, frequency, duration, route, startdate, stopDate,quantity)
                .setDataSource(getFormattedSql(sql, startDate, endDate),
                        connection);
        return jasperReport;
    }
}
