package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.MalariaConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "MalariaTestComparison")
@UsingDatasource(value = "openmrs")
public class MalariaTestComparison implements BaseReportTemplate<MalariaConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<MalariaConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {


        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> patientIdColumn = col.column("Patient ID", "Patient_ID", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> firstNameColumn = col.column("First Name", "First_Name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> lastNameColumn = col.column("Last Name", "Last_Name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age (in yrs)", "Age", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> testDateColumn = col.column("Test Date", "Test_Date", type.dateType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> rapidTestColumn = col.column("Rapid Test for Malaria", "RAPID_TEST_FOR_MALARIA", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> psForMpColumn = col.column("PS for MP", "PS_FOR_MP", type.stringType())
                .setStyle(columnStyle);


        String sql = getFileContent("sql/malariaTestComparison.sql");
        String paraCheck = reportConfig.getConfig().getParaCheck();
        String psForMp = reportConfig.getConfig().getPsForMp();

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)

                .setTemplate(Templates.reportTemplate)
                .setShowColumnTitle(true)
                .columns(patientIdColumn, firstNameColumn, lastNameColumn, genderColumn, ageColumn, testDateColumn, rapidTestColumn, psForMpColumn)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, paraCheck, psForMp, startDate, endDate),
                        connection);
        return jasperReport;
    }
}