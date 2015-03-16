package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilders;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "CodedObsCount")
@UsingDatasource("openmrs")
public class CodedObsCountTemplate implements BaseReportTemplate<CodedObsCountConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<CodedObsCountConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException {
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
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));
        String visitTypes = reportConfig.getConfig().getVisitTypes();
        String visitFilterTemplate = "on visit_type.type in (%s)";


        CrosstabRowGroupBuilder<String> visitRowGroup = ctab.rowGroup("visit", String.class)
                .setShowTotal(false);
        if (visitTypes != null) {
            crosstab.addRowGroup(visitRowGroup);
            visitFilterTemplate = String.format(visitFilterTemplate, visitTypes);
        }else{
            visitFilterTemplate = "";
        }

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        String sql = getFileContent("sql/codedObsCount.sql");

        String ageGroupName = reportConfig.getConfig().getAgeGroupName();
        String conceptNames = reportConfig.getConfig().getConceptNames();

        jasperReport.addTitle(cmp.horizontalList()
                        .add(cmp.text("Count of [ " + conceptNames + " ]")
                                .setStyle(Templates.boldStyle)
                                .setHorizontalAlignment(HorizontalAlignment.LEFT))
                        .newRow()
                        .add(cmp.verticalGap(10))
        );

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .summary(crosstab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, conceptNames, visitFilterTemplate, ageGroupName, startDate, endDate),
                        connection);
        return jasperReport;
    }
}
