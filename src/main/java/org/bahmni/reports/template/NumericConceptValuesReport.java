package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.DateConceptValuesConfig;
import org.bahmni.reports.model.NumericConceptValuesConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "NumericConceptValuesCount")
@UsingDatasource(value = "openmrs")
public class NumericConceptValuesReport implements BaseReportTemplate<NumericConceptValuesConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<NumericConceptValuesConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {


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

        String sql = getFileContent("sql/numericConceptValuesCount.sql");
        String ageGroupName = reportConfig.getConfig().getAgeGroupName();
        String rangeGroupName = reportConfig.getConfig().getRangeGroupName();
        String conceptNames = reportConfig.getConfig().getConceptNames();

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)

                .setTemplate(Templates.reportTemplate)
                .setShowColumnTitle(false)
                .columns(numericValueRange, reportAgeGroup, femaleCount, maleCount, otherCount, totalCount)
                .groupBy(numericValueGroup)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, rangeGroupName, ageGroupName, conceptNames, startDate, endDate),
                        connection);
        return jasperReport;
    }
}