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
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class CodedObsCountTemplate extends BaseReportTemplate<ObsCountConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsCountConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
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

        CrosstabBuilder crosstab = ctab.crosstab()
                .rowGroups(sortOrderGroup, ageRowGroup)
                .columnGroups(columnGroupQuestions, columnGroupAnswers)
                .measures(
                        ctab.measure("F", "female", Integer.class, Calculation.SUM),
                        ctab.measure("M", "male", Integer.class, Calculation.SUM),
                        ctab.measure("O", "other", Integer.class, Calculation.SUM),
                        ctab.measure("Total", "total", Integer.class, Calculation.SUM)
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
                        .add(cmp.text("Count of " + conceptNames.toString() )
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
        return jasperReport;
    }

    private String getSqlString(ObsCountConfig reportConfig, String startDate, String endDate) {
        String sql = getFileContent("sql/codedObsCount.sql");

        String visitTypes = SqlUtil.toCommaSeparatedSqlString(reportConfig.getVisitTypes());
        String visitFilterTemplate = "AND va.value_reference IN (%s)";

        if (StringUtils.isNotBlank(visitTypes)) {
            visitFilterTemplate = String.format(visitFilterTemplate, visitTypes);
        } else {
            visitFilterTemplate = "";
        }


        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("conceptNames", SqlUtil.toCommaSeparatedSqlString(reportConfig.getConceptNames()));
        sqlTemplate.add("reportGroupName", reportConfig.getAgeGroupName());
        sqlTemplate.add("visitFilter", visitFilterTemplate);

        if("false".equalsIgnoreCase(reportConfig.getCountOnlyClosedVisits())){
            sqlTemplate.add("endDateField", "obs.obs_datetime");
        }else{
            sqlTemplate.add("endDateField", "v.date_stopped");
        }

        if("false".equalsIgnoreCase((reportConfig.getCountOncePerPatient()))){
            sqlTemplate.add("countOncePerPatientClause", "SUM");
        }else{
            sqlTemplate.add("countOncePerPatientClause", "");
        }

        return sqlTemplate.render();
    }
}
