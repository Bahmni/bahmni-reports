package org.bahmni.reports.template;


import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.OrderType;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang3.StringUtils;
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
public class ObsCountTemplate extends BaseReportTemplate<ObsCountConfig> {

    private final String VISIT_TYPE_CRITERIA = "and va.value_reference in (%s)";

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsCountConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        CommonComponents.addTo(jasperReport, report, pageType);

        CrosstabRowGroupBuilder<String> ageGroup = ctab.rowGroup("base_age_group", String.class)
                .setShowTotal(false);

        CrosstabRowGroupBuilder<Integer> sortOrderGroup = ctab.rowGroup("base_sort_order", Integer.class)
                .setShowTotal(false)
                .setHeaderWidth(0)
                .setOrderType(OrderType.ASCENDING);

        CrosstabRowGroupBuilder<String> visitAttributeGroup = ctab.rowGroup("visit_type", String.class)
                .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("concept_name", String.class)
                .setShowTotal(false);

        CrosstabBuilder crosstab = ctab.crosstab()
                .columnGroups(columnGroup)
                .measures(
                        ctab.measure("Female", "female", Integer.class, Calculation.SUM),
                        ctab.measure("Male", "male", Integer.class, Calculation.SUM),
                        ctab.measure("Other", "other", Integer.class, Calculation.SUM),
                        ctab.measure("Total", "total", Integer.class, Calculation.SUM)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));

        String visitType = SqlUtil.toCommaSeparatedSqlString(report.getConfig().getVisitTypes());

        if (StringUtils.isNotBlank(visitType)) {
            crosstab = crosstab.rowGroups(sortOrderGroup, ageGroup, visitAttributeGroup);
            visitType = String.format(VISIT_TYPE_CRITERIA, visitType, sortOrderGroup);
        } else {
            crosstab = crosstab.rowGroups(sortOrderGroup, ageGroup);
            visitType = "";
        }

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        String sql = getFileContent("sql/obsCount.sql");

        String initialformattedSql = getFormattedSql(sql, report.getConfig(), visitType, startDate, endDate);
        jasperReport.addTitle(cmp.horizontalList()
                        .add(cmp.text("Count of " + report.getConfig().getConceptNames().toString())
                                .setStyle(Templates.boldStyle)
                                .setHorizontalAlignment(HorizontalAlignment.LEFT))
                        .newRow()
                        .add(cmp.verticalGap(10))
        );

        jasperReport.setColumnStyle(textStyle)
                .summary(crosstab)
                .setDataSource(initialformattedSql, connection);

        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, ObsCountConfig reportConfig, String visitType, String startDate, String endDate) {
        String locationTagNames = SqlUtil.toCommaSeparatedSqlString(reportConfig.getLocationTagNames());

        ST sqlTemplate = new ST(formattedSql, '#', '#');
        if ("false".equalsIgnoreCase(reportConfig.getCountOnlyClosedVisits())) {
            sqlTemplate.add("endDateField", "obs.obs_datetime");
        } else {
            sqlTemplate.add("endDateField", "v.date_stopped");
        }
        sqlTemplate.add("visitType", visitType);
        sqlTemplate.add("ageGroupName", reportConfig.getAgeGroupName());
        sqlTemplate.add("conceptNames", SqlUtil.toCommaSeparatedSqlString(reportConfig.getConceptNames()));
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if ("true".equalsIgnoreCase((reportConfig.getCountOncePerPatient()))) {
            sqlTemplate.add("countOncePerPatient", "");
        } else {
            sqlTemplate.add("countOncePerPatient", "SUM");
        }
        if (StringUtils.isNotBlank(locationTagNames)) {
            String countOnlyTaggedLocationsJoin = String.format("INNER JOIN " +
                    "(SELECT DISTINCT location_id " +
                    "FROM location_tag_map INNER JOIN location_tag ON location_tag_map.location_tag_id = location_tag.location_tag_id " +
                    " AND location_tag.name IN (%s)) locations ON locations.location_id = e.location_id", locationTagNames);
            sqlTemplate.add("countOnlyTaggedLocationsJoin", countOnlyTaggedLocationsJoin);
        } else {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", "");
        }
        return sqlTemplate.render();
    }
}
