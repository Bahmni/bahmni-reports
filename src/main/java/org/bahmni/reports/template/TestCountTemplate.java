package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.GroupHeaderLayout;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource(value = "openelis")
public class TestCountTemplate extends BaseReportTemplate<Config> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<Config> reportConfig, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        super.build(connection, jasperReport, reportConfig, startDate, endDate, resources, pageType);

        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> departmentColumn = col.column("Department", "department", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> testColumn = col.column("Test", "test", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<Integer> totalCountColumn = col.column("Total Count", "total_count", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> positiveCountColumn = col.column("Positive Count", "positive", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> negativeCountColumn = col.column("Negative Count", "negative", type.integerType())
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        ColumnGroupBuilder departmentGroup = grp.group(departmentColumn)
                .setTitleWidth(30)
                .setHeaderLayout(GroupHeaderLayout.VALUE)
                .showColumnHeaderAndFooter()
                .setPadding(30);

        String sql = getFileContent("sql/testCount.sql");

        jasperReport.setShowColumnTitle(false)
                .columns(departmentColumn, testColumn, totalCountColumn, positiveCountColumn, negativeCountColumn)
                .groupBy(departmentGroup)
                .setDataSource(String.format(sql, startDate, endDate),
                        connection);
        return jasperReport;
    }
}
