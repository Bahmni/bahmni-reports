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
import org.apache.commons.collections.CollectionUtils;
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
public class CodedObsCountTemplate extends BaseReportTemplate<ObsCountConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsCountConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        CrosstabRowGroupBuilder<String> ageRowGroup = ctab.rowGroup("age_group", String.class)
                .setShowTotal(false);

        CrosstabRowGroupBuilder<Integer> sortOrderGroup = ctab.rowGroup("sort_order", Integer.class)
                .setShowTotal(false)
                .setHeaderWidth(15)
                .setOrderType(OrderType.ASCENDING);

        CrosstabColumnGroupBuilder<String> columnGroupQuestions = ctab.columnGroup("concept_name", String.class)
                .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> columnGroupAnswers = ctab.columnGroup("answer_concept_name", String.class)
                .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> columnGroupGender = ctab.columnGroup("gender", String.class)
                .setShowTotal(true);

        CrosstabBuilder crosstab = ctab.crosstab()
                .rowGroups(sortOrderGroup, ageRowGroup)
                .columnGroups(columnGroupQuestions, columnGroupAnswers, columnGroupGender)
                .measures(
                        ctab.measure("", "total_count", Integer.class, Calculation.SUM)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()))
                .setCellWidth(75);

        CrosstabRowGroupBuilder<String> visitRowGroup = ctab.rowGroup("visit_type", String.class)
                .setShowTotal(false);
        List<String> visitTypes = report.getConfig().getVisitTypes();
        if (CollectionUtils.isNotEmpty(visitTypes)) {
            crosstab.addRowGroup(visitRowGroup);
        }

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());


        List<String> conceptNames = report.getConfig().getConceptNames();

        jasperReport.addTitle(cmp.horizontalList()
                        .add(cmp.text("Count of " + conceptNames.toString())
                                .setStyle(Templates.boldStyle)
                                .setHorizontalAlignment(HorizontalAlignment.LEFT))
                        .newRow()
                        .add(cmp.verticalGap(10))
        );
        String initialformattedSql = getSqlString(report.getConfig(), startDate, endDate);
        jasperReport.setColumnStyle(textStyle)
                .summary(crosstab)
                .setDataSource(initialformattedSql,
                        connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getSqlString(ObsCountConfig reportConfig, String startDate, String endDate) {
        String sql = getFileContent("sql/codedObsCount.sql");

        String visitTypes = SqlUtil.toCommaSeparatedSqlString(reportConfig.getVisitTypes());
        String visitFilterTemplate = "on visit_type.type in (%s)";

        if (StringUtils.isNotBlank(visitTypes)) {
            visitFilterTemplate = String.format(visitFilterTemplate, visitTypes);
        } else {
            visitFilterTemplate = "";
        }


        String conceptNames = SqlUtil.toCommaSeparatedSqlString(reportConfig.getConceptNames());
        String countOncePerPatientJoin = String.format("INNER JOIN (SELECT concept_id, person_id, max(obs_datetime) as datetime from obs " +
                "where obs.concept_id in (Select concept_id from concept_view where concept_full_name in (%s)) group by concept_id, " +
                "person_id) most_recent on most_recent.concept_id = obs.concept_id and most_recent.person_id = obs.person_id and " +
                "most_recent.datetime = obs.obs_datetime", conceptNames);

        String locationTagNames = SqlUtil.toCommaSeparatedSqlString(reportConfig.getLocationTagNames());
        String countOnlyTaggedLocationsJoin = String.format("INNER JOIN " +
                "(SELECT DISTINCT location_id " +
                "FROM location_tag_map INNER JOIN location_tag ON location_tag_map.location_tag_id = location_tag.location_tag_id " +
                " AND location_tag.name IN (%s)) locations ON locations.location_id = encounter.location_id", locationTagNames);

        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("conceptNames", conceptNames);
        sqlTemplate.add("reportGroupName", reportConfig.getAgeGroupName());
        sqlTemplate.add("visitFilter", visitFilterTemplate);
        if ("false".equalsIgnoreCase(reportConfig.getCountOnlyClosedVisits())) {
            sqlTemplate.add("endDateField", "obs.obs_datetime");
        } else {
            sqlTemplate.add("endDateField", "visit.date_stopped");
        }
        if ("true".equalsIgnoreCase((reportConfig.getCountOncePerPatient()))) {
            sqlTemplate.add("countOncePerPatientJoin", countOncePerPatientJoin);
        } else {
            sqlTemplate.add("countOncePerPatientJoin", "");
        }
        if (StringUtils.isNotBlank(locationTagNames)) {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", countOnlyTaggedLocationsJoin);
        } else {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", "");
        }
        return sqlTemplate.render();
    }
}
