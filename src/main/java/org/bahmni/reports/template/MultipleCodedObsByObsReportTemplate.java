package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.MultipleCodedObsByCodedObsReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class MultipleCodedObsByObsReportTemplate extends BaseReportTemplate<MultipleCodedObsByCodedObsReportConfig> {

    private static final List<String> FIXED_ROW_COLUMN_NAMES = Arrays.asList("gender", "age_group");

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<MultipleCodedObsByCodedObsReportConfig> report, String startDate, String endDate,
                                     List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        MultipleCodedObsByCodedObsReportConfig reportSpecificConfig = report.getConfig();

        List<String> rowsGroupBy = reportSpecificConfig.getRowsGroupBy();
        CrosstabRowGroupBuilder<String>[] rowGroups = new CrosstabRowGroupBuilder[rowsGroupBy.size()];
        int concept_index = 1;
        for (int i = 0; i < rowsGroupBy.size(); i++) {
            if (FIXED_ROW_COLUMN_NAMES.contains(rowsGroupBy.get(i))) {
                rowGroups[i] = ctab.rowGroup(rowsGroupBy.get(i), String.class).setShowTotal(false);
            } else {
                rowGroups[i] = ctab.rowGroup("concept" + concept_index + "_name", String.class).setShowTotal(false);
                concept_index++;
            }
        }

        List<String> columnsGroupBy = reportSpecificConfig.getColumnsGroupBy();
        CrosstabColumnGroupBuilder<String>[] columnGroups = new CrosstabColumnGroupBuilder[columnsGroupBy.size()];
        for (int i = 0; i < columnsGroupBy.size(); i++) {
            if (FIXED_ROW_COLUMN_NAMES.contains(columnsGroupBy.get(i))) {
                columnGroups[i] = ctab.columnGroup(columnsGroupBy.get(i), String.class).setShowTotal(false);
            } else {
                columnGroups[i] = ctab.columnGroup("concept" + concept_index + "_name", String.class).setShowTotal(false);
                concept_index++;
            }
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

        jasperReport.setColumnStyle(textStyle)
                .summary(crosstab)
                .setDataSource(getSqlString(reportSpecificConfig, startDate, endDate), connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getSqlString(MultipleCodedObsByCodedObsReportConfig reportConfig, String startDate, String endDate) {
        String sql = getFileContent("sql/multipleCodedObsByCodedObs.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);

        int index = 1;
        for (String conceptName : reportConfig.getRowsGroupBy()) {
            if (!FIXED_ROW_COLUMN_NAMES.contains(conceptName)) {
                sqlTemplate.add("concept" + index, conceptName);
                index++;
            }
        }
        for (String conceptName : reportConfig.getColumnsGroupBy()) {
            if (!FIXED_ROW_COLUMN_NAMES.contains(conceptName)) {
                sqlTemplate.add("concept" + index, conceptName);
                index++;
            }
        }

        sqlTemplate.add("reportGroupName", reportConfig.getAgeGroupName());
        return sqlTemplate.render();
    }

}
