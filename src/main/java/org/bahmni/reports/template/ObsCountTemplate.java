package org.bahmni.reports.template;


import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.CodedObsCountConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "obsCount")
@UsingDatasource("openmrs")
public class ObsCountTemplate implements BaseReportTemplate<CodedObsCountConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, Report<CodedObsCountConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("age_group", String.class)
                .setShowTotal(false);

        CrosstabRowGroupBuilder<String> visitAttributeGroup = ctab.rowGroup("visit_type", String.class)
                .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("concept_name", String.class)
                .setShowTotal(false);

        CrosstabBuilder crosstab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.text("Age Group / Outcome"))
                .rowGroups(rowGroup,visitAttributeGroup)
                .columnGroups(columnGroup)
                .measures(
                        ctab.measure("Female", "female", Integer.class, Calculation.SUM),
                        ctab.measure("Male", "male", Integer.class, Calculation.SUM)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        String sql = getFileContent("sql/obsCount.sql");

        String ageGroupName = reportConfig.getConfig().getAgeGroupName();
        String conceptNames = reportConfig.getConfig().getConceptNames();
        String formattedSql  = String.format(sql, ageGroupName, startDate, endDate, conceptNames);

        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .summary(crosstab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(formattedSql, connection);
        return report;
    }
}
