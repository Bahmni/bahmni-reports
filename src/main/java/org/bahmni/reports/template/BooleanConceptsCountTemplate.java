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
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class BooleanConceptsCountTemplate extends BaseReportTemplate<ObsCountConfig> {
    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsCountConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);
        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("concept_name", String.class)
                .setShowTotal(false);
        CrosstabRowGroupBuilder<String> booleanValueRowGroup = ctab.rowGroup("value_boolean", String.class)
                .setShowTotal(false);
        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("age_group", String.class)
                .setShowTotal(false);

        CrosstabBuilder crosstab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.horizontalList(DynamicReports.cmp.text("Boolean Concept Name").setStyle(Templates
                                .columnTitleStyle),
                        DynamicReports.cmp.text("Value").setStyle(Templates.columnTitleStyle)))
                .rowGroups(rowGroup, booleanValueRowGroup)
                .columnGroups(columnGroup)
                .measures(
                        ctab.measure("Female", "female", Integer.class, Calculation.SUM),
                        ctab.measure("Male", "male", Integer.class, Calculation.SUM),
                        ctab.measure("Other", "other", Integer.class, Calculation.SUM),
                        ctab.measure("Total", "total", Integer.class, Calculation.SUM)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        String sql = getFileContent("sql/booleanConceptsCount.sql");

        String initialformattedSql = getFormattedSql(sql, report.getConfig(), startDate, endDate);
        String formattedSql = appendCountOnceForPatient(initialformattedSql, report.getConfig());
        jasperReport.addTitle(cmp.horizontalList()
                        .add(cmp.text("Count of " + report.getConfig().getConceptNames().toString())
                                .setStyle(Templates.boldStyle)
                                .setHorizontalAlignment(HorizontalAlignment.LEFT))
                        .newRow()
                        .add(cmp.verticalGap(10))
        );

        jasperReport.setColumnStyle(textStyle)
                .summary(crosstab)
                .setDataSource(formattedSql, connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, ObsCountConfig reportConfig, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        if ("false".equalsIgnoreCase(reportConfig.getCountOnlyClosedVisits())) {
            sqlTemplate.add("endDateField", "obs.obs_datetime");
        } else {
            sqlTemplate.add("endDateField", "v.date_stopped");
        }
        sqlTemplate.add("ageGroupName", reportConfig.getAgeGroupName());
        sqlTemplate.add("conceptNames", SqlUtil.toCommaSeparatedSqlString(reportConfig.getConceptNames()));
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }

    private String appendCountOnceForPatient(String formattedSql, ObsCountConfig reportConfig) {
        String temp = "GROUP BY age_group, concept_name, female, male, other, ";
        if ("true".equalsIgnoreCase((reportConfig.getCountOncePerPatient()))) {
            formattedSql = formattedSql + temp + "concept_id, person_id;";
        } else {
            formattedSql = formattedSql + temp + "encounter_id;";
        }
        return formattedSql;
    }
}
