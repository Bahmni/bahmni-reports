package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "XrayCount")
@UsingDatasource(value = "openmrs")
public class XrayCount implements BaseReportTemplate<Config>{
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<Config> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        TextColumnBuilder<String> xrayType = col.column("X-Ray Type", "xray_type", type.stringType());
        TextColumnBuilder<Integer> count = col.column("Count", "count", type.integerType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        String sql = getFileContent("sql/xrayCount.sql");

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .columns(xrayType, count)
                .setDataSource(String.format(sql, startDate, endDate),
                        connection);
        return jasperReport;
    }
}