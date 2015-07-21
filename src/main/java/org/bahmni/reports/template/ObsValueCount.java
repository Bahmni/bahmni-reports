package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import org.bahmni.reports.model.ObsValueCountConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class ObsValueCount extends BaseReportTemplate<ObsValueCountConfig> {
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsValueCountConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        super.build(connection, jasperReport, reportConfig, startDate, endDate, resources, pageType);

        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> conceptColumn = col.column("Concept Name", "Name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> valueColumn = col.column("Value", "Value", type.stringType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> countColumn = col.column("Count", "Count", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        ColumnGroupBuilder testGroup = grp.group(conceptColumn)
                .setTitleWidth(30)
                .setHeaderLayout(GroupHeaderLayout.VALUE)
                .showColumnHeaderAndFooter()
                .setPadding(30);

        String sql = getFileContent("sql/obsValueCount.sql");
        String conceptNames = reportConfig.getConfig().getConceptNames();

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);


        jasperReport.setShowColumnTitle(false)
                .columns(conceptColumn, valueColumn, countColumn)
                .groupBy(testGroup)
                .setDataSource(String.format(sql,conceptNames,startDate, endDate),
                        connection);
        return jasperReport;
    }
}
