package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.ObsCountByConceptClassConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource(value = "openmrs")
public class ObsCountByConceptClass extends BaseReportTemplate<ObsCountByConceptClassConfig> {
    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsCountByConceptClassConfig>
            report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        StyleBuilder subtotalStyle = stl.style().bold().setHorizontalAlignment(HorizontalAlignment.RIGHT);

        TextColumnBuilder<String> conceptName = col.column("Concept Name", "Concept_Name", type.stringType())
                .setStyle(textStyle)
                .setWidth(40)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> obsCount = col.column("Observations Count", "Count", type.integerType())
                .setStyle(textStyle)
                .setWidth(40)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);

        AggregationSubtotalBuilder<Integer> totalCount = sbt.sum(obsCount)
                .setLabel("Total")
                .setLabelStyle(subtotalStyle);

        String sql = getFileContent("sql/obsCountByConceptClass.sql");

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport
                .setColumnStyle(textStyle)
                .columns(conceptName, obsCount)
                .subtotalsAtSummary(totalCount)
                .setDataSource(getFormattedSql(sql, report.getConfig(), startDate, endDate),
                        connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, ObsCountByConceptClassConfig reportConfig, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("conceptClassNames", SqlUtil.toCommaSeparatedSqlString(reportConfig.getConceptClassNames()));
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }
}