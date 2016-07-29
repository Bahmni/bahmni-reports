package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.ProgramConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class ProgramStateCountTemplate extends BaseReportTemplate<ProgramConfig> {
    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ProgramConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        TextColumnBuilder<String> stateName = col.column("State Name", "state_name", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> countTotal = col.column("Number of Patients", "count_total", type.integerType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);



        String sql = getFileContent("sql/programStateCount.sql");
        jasperReport.setShowColumnTitle(true)
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
                .setColumnStyle(textStyle)
                .columns(stateName, countTotal)
                .setDataSource(getFormattedSql(sql, report.getConfig().getProgramName(), startDate, endDate), connection);

        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, String programName, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("programName", programName);
        return sqlTemplate.render();
    }
}
