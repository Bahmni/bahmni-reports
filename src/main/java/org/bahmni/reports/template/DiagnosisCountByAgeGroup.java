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
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class DiagnosisCountByAgeGroup extends BaseReportTemplate<DiagnosisReportConfig> {

    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<DiagnosisReportConfig> reportConfig,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, reportConfig, pageType);

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());
        StyleBuilder cellStyle = Templates.columnStyle.setBorder(Styles.pen());

        CrosstabRowGroupBuilder<String> diseaseNameRowGroup = ctab.rowGroup("disease", String.class).setHeaderStyle(textStyle)
                .setHeaderWidth(120)
                .setShowTotal(false);
        CrosstabRowGroupBuilder<String> icd10RowGroup = ctab.rowGroup("icd10_code", String.class).setHeaderStyle(textStyle)
                .setHeaderWidth(60)
                .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> ageColumnGroup = ctab.columnGroup("age_group", String.class).setTotalHeaderWidth(95)
                .setShowTotal(false);
        CrosstabColumnGroupBuilder<Integer> ageSortOrderColumnGroup = ctab.columnGroup("age_group_sort_order", Integer.class)
                .setShowTotal(false).setOrderType(OrderType.ASCENDING);

        CrosstabBuilder crossTab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.horizontalList(DynamicReports.cmp.text("Disease Name").setStyle(Templates
                                .columnTitleStyle).setWidth(120),
                        DynamicReports.cmp.text("ICD Code").setStyle(Templates.columnTitleStyle).setWidth(60)))
                .rowGroups(diseaseNameRowGroup, icd10RowGroup)
                .columnGroups(ageSortOrderColumnGroup, ageColumnGroup)
                .measures(
                        ctab.measure("F", "female", Integer.class, Calculation.NOTHING).setStyle(textStyle),
                        ctab.measure("M", "male", Integer.class, Calculation.NOTHING).setStyle(textStyle),
                        ctab.measure("O", "other", Integer.class, Calculation.NOTHING).setStyle(textStyle)
                )
                .setCellStyle(cellStyle).setCellWidth(95);

        String sql = getFileContent("sql/diagnosisCountByAgeGroup.sql");

        jasperReport.setColumnStyle(textStyle)
                .summary(crossTab)
                .setDataSource(getFormattedSql(sql, reportConfig.getConfig(), startDate, endDate), connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, DiagnosisReportConfig reportConfig, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');

        sqlTemplate.add("ageGroupName", reportConfig.getAgeGroupName(true));
        sqlTemplate.add("visitType", SqlUtil.toCommaSeparatedSqlString(reportConfig.getVisitTypes()));
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);

        String locationTagNames = SqlUtil.toCommaSeparatedSqlString(reportConfig.getLocationTagNames());
        String countOnlyTaggedLocationsJoin = String.format("INNER JOIN " +
                "(SELECT DISTINCT location_id " +
                        " FROM location_tag_map INNER JOIN location_tag ON location_tag_map.location_tag_id = location_tag.location_tag_id " +
                        " AND location_tag.name IN (%s)) locations ON locations.location_id = e.location_id", locationTagNames);

        if (StringUtils.isNotBlank(locationTagNames)) {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", countOnlyTaggedLocationsJoin);
        } else {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", "");
        }
        if (reportConfig.getIcd10ConceptSource() != null) {
            sqlTemplate.add("icd10ConceptSource", reportConfig.getIcd10ConceptSource());
        } else {
            sqlTemplate.add("icd10ConceptSource", "ICD 10 - WHO" );
        }
        return sqlTemplate.render();
    }
}
