package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.ObsCountConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class BooleanConceptsCountTemplate extends BaseReportTemplate<ObsCountConfig> {
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsCountConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        super.build(connection, jasperReport, reportConfig, startDate, endDate, resources, pageType);
        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("concept_name", String.class)
                .setShowTotal(false);
        CrosstabRowGroupBuilder<String> booleanValueRowGroup = ctab.rowGroup("value_boolean", String.class)
                .setShowTotal(false);
        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("age_group", String.class)
                .setShowTotal(false);

        CrosstabBuilder crosstab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.horizontalList(DynamicReports.cmp.text("Boolean Concept Name").setStyle(Templates.columnTitleStyle),
                        DynamicReports.cmp.text("Value").setStyle(Templates.columnTitleStyle)))
                .rowGroups(rowGroup,booleanValueRowGroup)
                .columnGroups(columnGroup)
                .measures(
                        ctab.measure("Female", "female", Integer.class, Calculation.SUM),
                        ctab.measure("Male", "male", Integer.class, Calculation.SUM),
                        ctab.measure("Other", "other", Integer.class, Calculation.SUM),
                        ctab.measure("Total", "total", Integer.class, Calculation.SUM)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        String sql = getFileContent ("sql/booleanConceptsCount.sql");

        String ageGroupName = reportConfig.getConfig().getAgeGroupName();
        String conceptNames = reportConfig.getConfig().getConceptNames();
        String formattedSql  = String.format(sql,ageGroupName, conceptNames, conceptNames, ageGroupName, startDate, endDate);

        formattedSql = getFormattedSql(formattedSql,reportConfig.getConfig());
        jasperReport.addTitle(cmp.horizontalList()
                        .add(cmp.text("Count of [ " + conceptNames + " ]")
                                .setStyle(Templates.boldStyle)
                                .setHorizontalAlignment(HorizontalAlignment.LEFT))
                        .newRow()
                        .add(cmp.verticalGap(10))
        );

        jasperReport.setColumnStyle(textStyle)
                .summary(crosstab)
                .setDataSource(formattedSql, connection);
        return jasperReport;
    }

    private String getFormattedSql(String formattedSql, ObsCountConfig reportConfig) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        if ("false".equalsIgnoreCase(reportConfig.getCountOnlyClosedVisits())) {
            sqlTemplate.add("endDateField", "obs.obs_datetime");
        } else {
            sqlTemplate.add("endDateField", "v.date_stopped");
        }
        return sqlTemplate.render();
    }
}
