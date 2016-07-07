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
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.CodedObsByCodedObsReportConfig;
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
public class CodedObsByObsReportTemplate extends BaseReportTemplate<CodedObsByCodedObsReportConfig> {
    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<CodedObsByCodedObsReportConfig>
            report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        CommonComponents.addTo(jasperReport, report, pageType);

        CodedObsByCodedObsReportConfig reportSpecificConfig = report.getConfig();

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
        subHeader.append(reportSpecificConfig.getRowsGroupBy().get(0)).append(" vs ").append(reportSpecificConfig.getColumnsGroupBy().get
                (0));
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

    private String getSqlString(CodedObsByCodedObsReportConfig reportConfig, String startDate, String endDate) {
        String sql = getFileContent("sql/codedObsByCodedObs.sql");
        String locationTagNames = SqlUtil.toCommaSeparatedSqlString(reportConfig.getLocationTagNames());
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("firstConcept", reportConfig.firstConcept());
        sqlTemplate.add("secondConcept", reportConfig.secondConcept());
        sqlTemplate.add("reportGroupName", reportConfig.getAgeGroupName());
        if (StringUtils.isNotBlank(locationTagNames)) {
            String countOnlyTaggedLocationsJoin = String.format("INNER JOIN " +
                    "(SELECT DISTINCT location_id " +
                    "FROM location_tag_map INNER JOIN location_tag ON location_tag_map.location_tag_id = location_tag.location_tag_id " +
                    " AND location_tag.name IN (%s)) locations ON locations.location_id = e.location_id", locationTagNames);
            sqlTemplate.add("countOnlyTaggedLocationsJoin", countOnlyTaggedLocationsJoin);
        } else {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", "");
        }
        return sqlTemplate.render();
    }

}
