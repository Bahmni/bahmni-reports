package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.MalariaConfig;
import org.bahmni.reports.model.PatientsWithAbnormalLabtestResultsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "PatientsWithAbnormalLabtestResults")
@UsingDatasource(value = "openmrs")
public class PatientsWithAbnormalLabtestResults implements BaseReportTemplate<PatientsWithAbnormalLabtestResultsConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<PatientsWithAbnormalLabtestResultsConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {


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
        TextColumnBuilder<String> testNameColumn = col.column("Test Name", "test_name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> testResultColumn = col.column("Test Result", "test_result", type.stringType())
                .setStyle(columnStyle);


        String sql = getFileContent("sql/patientsWithAbnormalLabtestResults.sql");
        String conceptNames = reportConfig.getConfig().getConceptNames();


        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)

                .setTemplate(Templates.reportTemplate)
                .setShowColumnTitle(true)
                .columns(patientIdColumn, firstNameColumn, lastNameColumn, genderColumn, ageColumn, testNameColumn, testResultColumn)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, conceptNames, startDate, endDate, startDate, endDate),
                        connection);
        return jasperReport;
    }
}