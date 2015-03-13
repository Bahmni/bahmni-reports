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
import org.bahmni.reports.model.CodedObsByCodedObsReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "CodedObsByCodedObs")
@UsingDatasource("openmrs")
public class CodedObsByObsReportTemplate implements BaseReportTemplate<CodedObsByCodedObsReportConfig> {
    @Override
    public JasperReportBuilder build(Connection connection, Report<CodedObsByCodedObsReportConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        String sql = getFileContent("sql/codedObsByCodedObs.sql");

        CodedObsByCodedObsReportConfig reportSpecificConfig = reportConfig.getConfig();

        List<String> columnsGroupBy = reportSpecificConfig.getColumnsGroupBy();
        CrosstabColumnGroupBuilder<String>[] columnGroups = new CrosstabColumnGroupBuilder[columnsGroupBy.size()];
        for (int i = 0; i < reportSpecificConfig.getColumnsGroupBy().size(); i++) {
            columnGroups[i] = ctab.columnGroup(reportSpecificConfig
                    .getColumnName(columnsGroupBy.get(i)), String.class)
                    .setShowTotal(false);
        }


        List<String> rowsGroupBy = reportSpecificConfig.getRowsGroupBy();
        CrosstabRowGroupBuilder<String>[] rowGroups = new CrosstabRowGroupBuilder[rowsGroupBy.size()];
        for (int i = 0; i < reportSpecificConfig.getRowsGroupBy().size(); i++) {
            rowGroups[i] = ctab.rowGroup(reportSpecificConfig
                    .getColumnName(rowsGroupBy.get(i)), String.class)
                    .setShowTotal(false);
        }

        CrosstabBuilder crosstab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.text("Age Group / Outcome"))
                .rowGroups(rowGroups)
                .columnGroups(columnGroups)
                .measures(
                        ctab.measure(null, "patient_count", Integer.class, Calculation.SUM)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        JasperReportBuilder report = report();

        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .summary(crosstab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql,
                        reportSpecificConfig.firstConcept(),
                        reportSpecificConfig.secondConcept(),
                        reportSpecificConfig.getAgeGroupName(),
                        reportSpecificConfig.firstConcept(),
                        startDate,
                        endDate,
                        reportSpecificConfig.secondConcept(),
                        startDate,
                        endDate
                ), connection);
        return report;
    }
}
