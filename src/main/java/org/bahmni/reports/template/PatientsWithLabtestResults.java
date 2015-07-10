package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.PatientsWithLabtestResultsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "PatientsWithLabtestResults")
@UsingDatasource(value = "openmrs")
public class PatientsWithLabtestResults extends BaseReportTemplate<PatientsWithLabtestResultsConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<PatientsWithLabtestResultsConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        super.build(connection, jasperReport, reportConfig, startDate, endDate, resources, pageType);

        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> patientIdColumn = col.column("Patient ID", "patient_id", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> firstNameColumn = col.column("First Name", "first_name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> lastNameColumn = col.column("Last Name", "last_name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "gender", type.stringType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age (in yrs)", "age", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> testDateColumn = col.column("Test Date", "test_date", type.dateType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> testNameColumn = col.column("Test Name", "test_name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> testResultColumn = col.column("Test Result", "test_result", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> abnormalityColumn = col.column("Test Outcome", "test_outcome", type.stringType())
                .setStyle(columnStyle);


        String sql = getFileContent("sql/patientsWithLabtestResults.sql");
        String conceptNames = reportConfig.getConfig().getConceptNames();
        String testOutcome = reportConfig.getConfig().getTestOutcome();

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setShowColumnTitle(true)
                .columns(patientIdColumn, firstNameColumn, lastNameColumn, genderColumn, ageColumn, testDateColumn, testNameColumn, testResultColumn,abnormalityColumn)
                .setDataSource(String.format(sql, conceptNames, startDate, endDate, startDate, endDate, testOutcome),
                        connection);
        return jasperReport;
    }
}