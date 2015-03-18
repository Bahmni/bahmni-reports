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
import org.bahmni.reports.model.MultipleCodedObsByCodedObsReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "MultipleCodedObsByCodedObs")
@UsingDatasource("openmrs")
public class MultipleCodedObsByObsReportTemplate implements BaseReportTemplate<MultipleCodedObsByCodedObsReportConfig> {

    private static final List<String> FIXED_COLUMNS = Arrays.asList("gender");

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<MultipleCodedObsByCodedObsReportConfig> reportConfig, String startDate, String endDate,
                                     List<AutoCloseable> resources) throws SQLException, DRException {

        MultipleCodedObsByCodedObsReportConfig reportSpecificConfig = reportConfig.getConfig();

        List<String> rowsGroupBy = reportSpecificConfig.getRowsGroupBy();
        CrosstabRowGroupBuilder<String>[] rowGroups = new CrosstabRowGroupBuilder[rowsGroupBy.size()];
        int concept_index = 1;
        for (int i = 0; i < rowsGroupBy.size(); i++, concept_index++) {
            rowGroups[i] = getRow(concept_index, rowsGroupBy.get(i));
        }

        List<String> columnsGroupBy = reportSpecificConfig.getColumnsGroupBy();
        CrosstabColumnGroupBuilder<String>[] columnGroups = new CrosstabColumnGroupBuilder[columnsGroupBy.size()];
        for (int i = 0; i < columnsGroupBy.size(); i++, concept_index++) {
            columnGroups[i] = getColumn(concept_index, columnsGroupBy.get(i));
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
        for (String rowName : reportSpecificConfig.getRowsGroupBy()) {
            subHeader.append("(").append(rowName).append(")").append(" vs ");
        }
        for (String columnName : reportSpecificConfig.getColumnsGroupBy()) {
            subHeader.append("(").append(columnName).append(")").append(" vs ");
        }
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
                .setDataSource(getSqlString(reportSpecificConfig, startDate, endDate), connection);
        return jasperReport;
    }

    private CrosstabColumnGroupBuilder<String> getColumn(int concept_index, String column) {
        if (FIXED_COLUMNS.contains(column)) {
            return ctab.columnGroup(column, String.class).setShowTotal(false);
        }
        return ctab.columnGroup("concept" + concept_index + "_name", String.class).setShowTotal(false);
    }

    private CrosstabRowGroupBuilder<String> getRow(int concept_index, String row) {
        if (FIXED_COLUMNS.contains(row)) {
            return ctab.rowGroup(row, String.class).setShowTotal(false);
        }
        return ctab.rowGroup("concept" + concept_index + "_name", String.class).setShowTotal(false);
    }

    private String getSqlString(MultipleCodedObsByCodedObsReportConfig reportConfig, String startDate, String endDate) {
        String sql = getFileContent("sql/multipleCodedObsByCodedObs.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);

        int index = 1;
        for (String conceptName : reportConfig.getRowsGroupBy()) {
            sqlTemplate.add("concept" + index, conceptName);
            index++;
        }
        for (String conceptName : reportConfig.getColumnsGroupBy()) {
            sqlTemplate.add("concept" + index, conceptName);
            index++;
        }

        sqlTemplate.add("reportGroupName", reportConfig.getAgeGroupName());
        return sqlTemplate.render();
    }

}
