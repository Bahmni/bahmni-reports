package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SqlReportConfig;
import org.bahmni.reports.util.CommonComponents;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

public class SqlReportTemplate extends BaseReportTemplate<SqlReportConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<SqlReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        String sqlString = getSqlString(report, startDate, endDate);
        ResultSet resultSet = null;
        Statement statement = null;
        ResultSetMetaData metaData;
        int columnCount;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlString);
            metaData = resultSet.getMetaData();
            columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            jasperReport.addColumn(col.column(metaData.getColumnLabel(i), metaData.getColumnLabel(i), type.stringType()));
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        jasperReport.setDataSource(resultSet);
        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);
        resources.add(statement);
        return jasperReport;
    }

    private String getSqlString(Report<SqlReportConfig> reportConfig, String startDate, String endDate) {
        String sql = getFileContent(reportConfig.getConfig().getSqlPath(), true);
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }

}
