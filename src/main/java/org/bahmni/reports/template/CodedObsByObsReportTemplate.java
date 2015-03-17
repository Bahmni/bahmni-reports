package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.CodedObsByCodedObsReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "CodedObsByCodedObs")
@UsingDatasource("openmrs")
public class CodedObsByObsReportTemplate implements BaseReportTemplate<CodedObsByCodedObsReportConfig> {
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<CodedObsByCodedObsReportConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {

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
                .rowGroups(rowGroups)
                .columnGroups(columnGroups)
                .measures(
                        ctab.measure(null, "patient_count", Integer.class, Calculation.SUM)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        StringBuilder subHeader = new StringBuilder();
        subHeader.append(reportSpecificConfig.getRowsGroupBy().get(0)).append(" vs ").append(reportSpecificConfig.getColumnsGroupBy().get(0));
        jasperReport.addTitle(cmp.horizontalList()
                        .add(cmp.text(subHeader.toString())
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
                .setDataSource(getSqlString(reportSpecificConfig,startDate, endDate), connection);
        return jasperReport;
    }

    private String getSqlString(CodedObsByCodedObsReportConfig reportConfig, String startDate, String endDate) {
        String sql = getFileContent("sql/codedObsByCodedObs.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("firstConcept", reportConfig.firstConcept());
        sqlTemplate.add("secondConcept", reportConfig.secondConcept());
        sqlTemplate.add("reportGroupName", reportConfig.getAgeGroupName());
        return sqlTemplate.render();
    }

}
