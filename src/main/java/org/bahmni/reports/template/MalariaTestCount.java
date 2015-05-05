package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.MalariaConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "MalariaTestCount")
@UsingDatasource(value = "openmrs")
public class MalariaTestCount implements BaseReportTemplate<MalariaConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<MalariaConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {


        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> testColumn = col.column("Malaria Test", "Malaria_Test", type.stringType())
                .setStyle(columnStyle);
        TextColumnBuilder<String> resultTypeColumn = col.column("Result type", "Result_Type", type.stringType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> countColumn = col.column("Count", "Count", type.integerType())
                .setStyle(columnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        ColumnGroupBuilder testGroup = grp.group(testColumn)
                .setTitleWidth(30)
                .setHeaderLayout(GroupHeaderLayout.VALUE)
                .showColumnHeaderAndFooter()
                .setPadding(30);

        String sql = getFileContent("sql/malariaTestCount.sql");
        String paraCheck = reportConfig.getConfig().getParaCheck();
        String psForMp = reportConfig.getConfig().getPsForMp();

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)

                .setTemplate(Templates.reportTemplate)
                .setShowColumnTitle(false)
                .columns(testColumn, resultTypeColumn, countColumn)
                .groupBy(testGroup)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, paraCheck, startDate, endDate, psForMp, startDate,
                                endDate, paraCheck, psForMp, startDate, endDate),
                        connection);
        return jasperReport;
    }
}