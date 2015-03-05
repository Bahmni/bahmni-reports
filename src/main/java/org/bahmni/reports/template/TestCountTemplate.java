package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.GroupHeaderLayout;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.ReportConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.grp;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "TestCount")
public class TestCountTemplate implements BaseReportTemplate {

    @Autowired
    private DataSource openelisDataSource;

    @Override
    public JasperReportBuilder build(ReportConfig reportConfig, String startDate, String endDate) throws SQLException {

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

        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setTemplate(Templates.reportTemplate)
                .setShowColumnTitle(false)
                .columns(departmentColumn, testColumn, totalCountColumn, positiveCountColumn, negativeCountColumn)
                .groupBy(departmentGroup)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, startDate, endDate),
                        openelisDataSource.getConnection());
        return report;
    }
}
