package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.model.VisitAggregateCountConfig;
import org.bahmni.reports.util.CommonComponents;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class VisitAggregateCountReportTemplate extends BaseReportTemplate<VisitAggregateCountConfig> {
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<VisitAggregateCountConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

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
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            boolean hasMoreResultSets = stmt.execute(sqlString);
            while (hasMoreResultSets || stmt.getUpdateCount() != -1) {
                if (hasMoreResultSets) {
                    ResultSet rs = stmt.getResultSet();
                    if (rs.isBeforeFirst()) {
                        jasperReport.setDataSource(rs);
                        return jasperReport;
                    }
                }
                hasMoreResultSets = stmt.getMoreResults();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jasperReport;
    }

    private String getSqlString(String visitTypes, String startDate, String endDate) {
        String sql = getFileContent("sql/visitAggregateCount.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("visitTypes", visitTypes);
        return sqlTemplate.render();
    }

}
