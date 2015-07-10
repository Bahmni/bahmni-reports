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
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.CodedObsCountConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "CodedObsCount")
@UsingDatasource("openmrs")
public class CodedObsCountTemplate extends BaseReportTemplate<CodedObsCountConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<CodedObsCountConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        super.build(connection, jasperReport, reportConfig, startDate, endDate, resources, pageType);

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

        CrosstabRowGroupBuilder<String> visitRowGroup = ctab.rowGroup("visit", String.class)
                .setShowTotal(false);
        String visitTypes = reportConfig.getConfig().getVisitTypes();
        if (visitTypes != null) {
            crosstab.addRowGroup(visitRowGroup);
        }

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());


        String conceptNames = reportConfig.getConfig().getConceptNames();

        jasperReport.addTitle(cmp.horizontalList()
                        .add(cmp.text("Count of [ " + conceptNames + " ]")
                                .setStyle(Templates.boldStyle)
                                .setHorizontalAlignment(HorizontalAlignment.LEFT))
                        .newRow()
                        .add(cmp.verticalGap(10))
        );

        jasperReport.setColumnStyle(textStyle)
                .summary(crosstab)
                .setDataSource(getSqlString(reportConfig.getConfig(), startDate, endDate),
                        connection);
        return jasperReport;
    }

    private String getSqlString(CodedObsCountConfig reportConfig, String startDate, String endDate) {
        String sql = getFileContent("sql/codedObsCount.sql");

        String visitTypes = reportConfig.getVisitTypes();
        String visitFilterTemplate = "on visit_type.type in (%s)";

        if (visitTypes != null) {
            visitFilterTemplate = String.format(visitFilterTemplate, visitTypes);
        } else {
            visitFilterTemplate = "";
        }


        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("conceptNames", reportConfig.getConceptNames());
        sqlTemplate.add("reportGroupName", reportConfig.getAgeGroupName());
        sqlTemplate.add("visitFilter", visitFilterTemplate);
        return sqlTemplate.render();
    }

}
