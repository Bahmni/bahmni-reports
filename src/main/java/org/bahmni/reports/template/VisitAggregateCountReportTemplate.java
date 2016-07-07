package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.model.VisitAggregateCountConfig;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class VisitAggregateCountReportTemplate extends BaseReportTemplate<VisitAggregateCountConfig> {
    private VisitAggregateCountConfig reportConfig;
    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<VisitAggregateCountConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {

        this.reportConfig = report.getConfig();
        CommonComponents.addTo(jasperReport, report, pageType);

        TextColumnBuilder<String> visitTypeColumn = col.column("Visit Type", "visit_type", type.stringType()).setStyle(minimalColumnStyle)
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> admittedColumn = col.column("Admitted", "admitted", type.stringType()).setStyle(minimalColumnStyle)
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> dischargedColumn = col.column("Discharged", "discharged", type.stringType()).setStyle(minimalColumnStyle)
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        jasperReport.setShowColumnTitle(true)
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
                .columns(visitTypeColumn, admittedColumn, dischargedColumn);
        String visitTypes = report.getConfig().getVisitTypes();
        String sqlString = getSqlString(visitTypes, startDate, endDate);

        JasperReportBuilder jasperReportBuilder = SqlUtil.executeReportWithStoredProc(jasperReport, connection, sqlString);
        return new BahmniReportBuilder(jasperReportBuilder);
    }

    private String getSqlString(String visitTypes, String startDate, String endDate) {
        String sql = getFileContent("sql/visitAggregateCount.sql");
        String locationTagNames = SqlUtil.toCommaSeparatedSqlString(reportConfig.getLocationTagNames());
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("visitTypes", visitTypes);
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
