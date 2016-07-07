package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.ProgramStateTransitionConfig;
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
public class ProgramStateTransitionTemplate extends BaseReportTemplate<ProgramStateTransitionConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<ProgramStateTransitionConfig> report, String startDate, String endDate, List<AutoCloseable>
                                                 resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        StyleBuilder subtotalStyle = stl.style().bold().setHorizontalAlignment(HorizontalAlignment.RIGHT);

        TextColumnBuilder<String> stateFromName = col.column("From", "state_from_name", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.LEFT);
        TextColumnBuilder<String> stateToName = col.column("To", "state_to_name", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.LEFT);
        TextColumnBuilder<Integer> countTotal = col.column("Number of Patients", "count_total", type.integerType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.LEFT);

        AggregationSubtotalBuilder<Integer> totalCount = sbt.sum(countTotal)
                .setLabel("Total")
                .setLabelStyle(subtotalStyle);

        String sql = getFileContent("sql/programStateTransition.sql");

        jasperReport.setShowColumnTitle(true)
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
                .setColumnStyle(textStyle)
                .columns(stateFromName, stateToName, countTotal)
                .subtotalsAtSummary(totalCount)
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
