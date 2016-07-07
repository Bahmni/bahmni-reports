package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource(value = "openmrs")
public class IpdOpdVisitCount extends BaseReportTemplate<Config> {
    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<Config> report, String startDate,
                                     String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

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

        jasperReport.setColumnStyle(textStyle)
                .columns(newOpd, oldOpd, totalOpd, newIpd, oldIpd, totalIpd)
                .setDataSource(getFormattedSql(sql, startDate, endDate),
                        connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }
}