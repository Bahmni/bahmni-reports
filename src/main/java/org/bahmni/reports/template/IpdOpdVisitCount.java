package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.ObsTemplateConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "IpdOpdVisitCount")
@UsingDatasource(value = "openmrs")
public class IpdOpdVisitCount implements BaseReportTemplate<Config>{
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<Config> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        TextColumnBuilder<String> newOpd = col.column("New OPD", "New_OPD", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> oldOpd = col.column("Old OPD", "Old_OPD", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> totalOpd = col.column("Total OPD", "Total_OPD", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> newIpd = col.column("New IPD", "New_IPD", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> oldIpd = col.column("Old IPD", "Old_IPD", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> totalIpd = col.column("Total IPD", "Total_IPD", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        String sql = getFileContent("sql/ipdOpdVisitCount.sql");

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .columns(newOpd, oldOpd, totalOpd, newIpd, oldIpd, totalIpd)
                .setDataSource(String.format(sql, startDate, endDate),
                        connection);
        return jasperReport;
    }
}