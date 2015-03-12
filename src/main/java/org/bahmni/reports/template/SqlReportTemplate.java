package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SqlReportConfig;

import java.sql.*;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

public class SqlReportTemplate  implements BaseReportTemplate<SqlReportConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, Report<SqlReportConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        String sql = getFileContent(reportConfig.getConfig().getSqlPath(), true);
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
