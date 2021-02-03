package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.PatientsWithLabtestResultsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

//import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource(value = "openmrs")
public class PatientsWithLabtestResults extends BaseReportTemplate<PatientsWithLabtestResultsConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<PatientsWithLabtestResultsConfig>
            report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        TextColumnBuilder<String> patientIdColumn = col.column("Patient ID", "patient_id", type.stringType())
                .setStyle(minimalColumnStyle);
        TextColumnBuilder<String> firstNameColumn = col.column("First Name", "first_name", type.stringType())
                .setStyle(minimalColumnStyle);
        TextColumnBuilder<String> lastNameColumn = col.column("Last Name", "last_name", type.stringType())
                .setStyle(minimalColumnStyle);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "gender", type.stringType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age (in yrs)", "age", type.integerType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> testDateColumn = col.column("Test Date", "test_date", type.dateType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> testNameColumn = col.column("Test Name", "test_name", type.stringType())
                .setStyle(minimalColumnStyle);
        TextColumnBuilder<String> testResultColumn = col.column("Test Result", "test_result", type.stringType())
                .setStyle(minimalColumnStyle);
        TextColumnBuilder<String> abnormalityColumn = col.column("Test Outcome", "test_outcome", type.stringType())
                .setStyle(minimalColumnStyle);


        String sql = getFileContent("sql/patientsWithLabtestResults.sql");

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setShowColumnTitle(true)
                .columns(patientIdColumn, firstNameColumn, lastNameColumn, genderColumn, ageColumn, testDateColumn, testNameColumn,
                        testResultColumn, abnormalityColumn)
                .setDataSource(getFormattedSql(sql, report.getConfig(), startDate, endDate),
                        connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, PatientsWithLabtestResultsConfig reportConfig, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("conceptNames", SqlUtil.toCommaSeparatedSqlString(reportConfig.getConceptNames()));
        sqlTemplate.add("testOutcome", SqlUtil.toCommaSeparatedSqlString(reportConfig.getTestOutcome()));
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }
}