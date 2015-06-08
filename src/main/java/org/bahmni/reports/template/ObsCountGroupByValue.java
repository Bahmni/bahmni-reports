package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.*;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.CodedObsByCodedObsReportConfig;
import org.bahmni.reports.model.ObsCountGroupByValueConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "ObsCountGroupByValue")
@UsingDatasource("openmrs")
public class ObsCountGroupByValue implements BaseReportTemplate<ObsCountGroupByValueConfig> {
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsCountGroupByValueConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {

        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> conceptColumn = col.column("Concept Name", "Name", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> valueColumn = col.column("Obs Value", "Value", type.stringType())
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

        String sql = getFileContent("sql/obsCountGroupByValue.sql");
        String conceptNames = reportConfig.getConfig().getConceptNames();

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.addTitle(cmp.horizontalList()
                        .add(cmp.text("Count of [ " + conceptNames + " ]")
                                .setStyle(Templates.boldStyle)
                                .setHorizontalAlignment(HorizontalAlignment.LEFT))
                        .newRow()
                        .add(cmp.verticalGap(10))
        );
        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setTemplate(Templates.reportTemplate)
                .setShowColumnTitle(false)
                .columns(conceptColumn, valueColumn, countColumn)
                .groupBy(testGroup)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql,conceptNames,startDate, endDate),
                        connection);
        return jasperReport;

//        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("Name", String.class)
//                .setShowTotal(false);
//        CrosstabRowGroupBuilder<String> valueRowGroup = ctab.rowGroup("Value", String.class)
//                .setShowTotal(false);
//        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("Count", String.class)
//                .setShowTotal(false);
////        TextColumnBuilder<Integer> countColumn = col.column("Count", "Count", type.integerType())
////                .setStyle(stl.style().setRightBorder(stl.pen1Point()))
////                .setHorizontalAlignment(HorizontalAlignment.CENTER);
//
//        CrosstabBuilder crosstab = ctab.crosstab()
//                .headerCell(DynamicReports.cmp.horizontalList(DynamicReports.cmp.text("Value").setStyle(Templates.columnTitleStyle),
//                        DynamicReports.cmp.text("Value").setStyle(Templates.columnTitleStyle)))
//                .rowGroups(rowGroup, valueRowGroup)
//                .columnGroups(columnGroup)
//                .measures(
//                        ctab.measure("Count", "Count", Integer.class, Calculation.SUM))
//                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));
//
//        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());
//
//        String sql = getFileContent ("sql/obsCountGroupByValue.sql");
//
//        String conceptNames = reportConfig.getConfig().getConceptNames();
//        String formattedSql  = String.format(sql, conceptNames, startDate, endDate);
//
//        jasperReport.addTitle(cmp.horizontalList()
//                        .add(cmp.text("Count of [ " + conceptNames + " ]")
//                                .setStyle(Templates.boldStyle)
//                                .setHorizontalAlignment(HorizontalAlignment.LEFT))
//                        .newRow()
//                        .add(cmp.verticalGap(10))
//        );
//
//        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
//                .setColumnStyle(textStyle)
//                .setTemplate(Templates.reportTemplate)
////                .columns(countColumn)
//                .setReportName(reportConfig.getName())
//                .summary(crosstab)
//                .pageFooter(Templates.footerComponent)
//                .setDataSource(formattedSql, connection);
//        return jasperReport;
    }
}
