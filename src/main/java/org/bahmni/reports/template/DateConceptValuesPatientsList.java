package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.DateConceptValuesConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "DateConceptValuesPatientsList")
@UsingDatasource(value = "openmrs")
public class DateConceptValuesPatientsList extends BaseReportTemplate<DateConceptValuesConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<DateConceptValuesConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        super.build(connection, jasperReport, reportConfig, startDate, endDate, resources, pageType);

        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> patientIdentifier = col.column("Patient ID", "identifier", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> givenName = col.column("First Name", "given_name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> familyName = col.column("Last Name", "family_name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> gender = col.column("Gender", "gender", type.stringType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> age = col.column("Age (in years)", "age", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> dateValue = col.column("Date Value", "date_value", type.dateType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);


        String sql = getFileContent("sql/dateConceptValuesPatientsList.sql");
        String templateName = reportConfig.getConfig().getTemplateName();
        String conceptNames = reportConfig.getConfig().getConceptNames();

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);
        
        jasperReport.setShowColumnTitle(true)
                .columns(patientIdentifier, givenName, familyName, gender, age, dateValue)
                .setDataSource(String.format(sql, templateName, conceptNames, startDate, endDate),
                        connection);
        return jasperReport;
    }
}