package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.DateConceptValuesConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource(value = "openmrs")
public class DateConceptValuesPatientsList extends BaseReportTemplate<DateConceptValuesConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<DateConceptValuesConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        TextColumnBuilder<String> patientIdentifier = col.column("Patient ID", "identifier", type.stringType())
                .setStyle(minimalColumnStyle);
        TextColumnBuilder<String> givenName = col.column("First Name", "given_name", type.stringType())
                .setStyle(minimalColumnStyle);
        TextColumnBuilder<String> familyName = col.column("Last Name", "family_name", type.stringType())
                .setStyle(minimalColumnStyle);
        TextColumnBuilder<String> gender = col.column("Gender", "gender", type.stringType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> age = col.column("Age (in years)", "age", type.integerType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> dateValue = col.column("Date Value", "date_value", type.dateType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);


        String sql = getFileContent("sql/dateConceptValuesPatientsList.sql");

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setShowColumnTitle(true)
                .columns(patientIdentifier, givenName, familyName, gender, age, dateValue)
                .setDataSource(getFormattedSql(sql, report.getConfig(), startDate, endDate),
                        connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, DateConceptValuesConfig reportConfig, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("templateName", reportConfig.getTemplateName());
        sqlTemplate.add("conceptNames", SqlUtil.toCommaSeparatedSqlString(reportConfig.getConceptNames()));
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }
}