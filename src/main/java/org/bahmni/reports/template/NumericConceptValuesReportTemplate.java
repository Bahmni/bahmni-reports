package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import org.bahmni.reports.model.NumericConceptValuesConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource(value = "openmrs")
public class NumericConceptValuesReportTemplate extends BaseReportTemplate<NumericConceptValuesConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<NumericConceptValuesConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        super.build(connection, jasperReport, reportConfig, startDate, endDate, resources, pageType);

        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> numericValueRange = col.column("Numeric Value Range", "numeric_value_range", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> reportAgeGroup = col.column("Report Age Group", "report_age_group", type.stringType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> femaleCount = col.column("Female", "female", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> maleCount = col.column("Male", "male", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> otherCount = col.column("Other", "other", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> totalCount = col.column("Total", "total", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        ColumnGroupBuilder numericValueGroup = grp.group(numericValueRange)
                .setTitleWidth(30)
                .setHeaderLayout(GroupHeaderLayout.VALUE)
                .showColumnHeaderAndFooter()
                .setPadding(30);

        String ageGroupName = reportConfig.getConfig().getAgeGroupName();
        String rangeGroupName = reportConfig.getConfig().getRangeGroupName();
        String conceptNames = reportConfig.getConfig().getConceptNames();
        Boolean countOncePerPatient = reportConfig.getConfig().getCountOncePerPatient();
        String sqlString = getSqlString(ageGroupName, rangeGroupName, conceptNames, countOncePerPatient, startDate, endDate);

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setShowColumnTitle(false)
                .groupBy(numericValueGroup)
                .columns(reportAgeGroup, femaleCount, maleCount, otherCount, totalCount)
                .setDataSource(sqlString, connection);

        return jasperReport;
    }

    private String getSqlString(String ageGroupName, String rangeGroupName, String conceptNames, Boolean countOncePerPatient, String startDate, String endDate) {
        String sql = getFileContent("sql/numericConceptValuesCount.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("ageGroupName", ageGroupName);
        sqlTemplate.add("rangeGroupName", rangeGroupName);
        sqlTemplate.add("conceptNames", conceptNames);
        sqlTemplate.add("countOncePerPatient", countOncePerPatient);
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);

        return sqlTemplate.render();
    }
}