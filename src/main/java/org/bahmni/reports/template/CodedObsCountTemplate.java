package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.OrderType;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.ReportConfig;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "CodedObsCount")
public class CodedObsCountTemplate extends AbstractMRSReportTemplate {

    @Override
    public JasperReportBuilder buildReport(Connection connection, ReportConfig reportConfig, String startDate, String endDate) throws SQLException {
        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("age_group", String.class)
                .setShowTotal(false);

        CrosstabRowGroupBuilder<Integer> sortOrderGroup = ctab.rowGroup("sort_order", Integer.class)
                .setShowTotal(false)
                .setHeaderWidth(15)
                .setOrderType(OrderType.ASCENDING);

        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("concept_name", String.class)
                .setShowTotal(false);

        CrosstabBuilder crosstab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.text("Age Group / Outcome"))
                .rowGroups(sortOrderGroup, rowGroup)
                .columnGroups(columnGroup)
                .measures(
                        ctab.measure("Female", "female_count", Integer.class, Calculation.NOTHING),
                        ctab.measure("Male", "male_count", Integer.class, Calculation.NOTHING),
                        ctab.measure("Other", "other_count", Integer.class, Calculation.NOTHING),
                        ctab.measure("Total", "total_count", Integer.class, Calculation.NOTHING)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        String sql = getFileContent("sql/codedObsCount.sql");

        String ageGroupName = reportConfig.getAgeGroupName();
        String conceptName = reportConfig.getConceptName();

        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .summary(crosstab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, conceptName, startDate, endDate, ageGroupName),
                        connection);
        return report;
    }

}
