package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.ReportConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

public abstract class BaseReportTemplate {
    public abstract DataSource getDataSource();

    public JasperReportBuilder build(Connection connection, ReportConfig reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        return buildReport(connection, reportConfig, startDate, endDate, resources);
    }

    protected abstract JasperReportBuilder buildReport(Connection connection, ReportConfig reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException;

    protected JasperReportBuilder buildGenericReport(Connection connection, ReportConfig reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        String sql = getFileContent(reportConfig.getSqlPath(), true);
        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent);


        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(String.format(sql, startDate, endDate));
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for(int i = 1; i <= columnCount; i++) {
            report.addColumn(col.column(metaData.getColumnLabel(i), metaData.getColumnName(i), type.stringType()));
        }

        report.setDataSource(resultSet);
        resources.add(statement);
        return report;
    }
}
