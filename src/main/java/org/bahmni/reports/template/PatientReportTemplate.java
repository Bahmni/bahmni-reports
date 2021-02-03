package org.bahmni.reports.template;


import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.stringtemplate.v4.ST;

import java.sql.*;
import java.util.List;

//import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class PatientReportTemplate extends BaseReportTemplate<Config> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<Config> report, String startDate,
                                     String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        String sql = getFormattedSql(getFileContent("sql/personReport.sql"), startDate, endDate);
        Statement stmt;
        ResultSetMetaData metaData;
        int columnCount;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            boolean hasMoreResultSets = stmt.execute(sql);
            while (hasMoreResultSets ||
                    stmt.getUpdateCount() != -1) { //if there are any more queries to be processed
                if (hasMoreResultSets) {
                    rs = stmt.getResultSet();

                    if (rs.isBeforeFirst()) {
                        jasperReport.setDataSource(rs);
                        break;
                    }
                }
                hasMoreResultSets = stmt.getMoreResults(); //true if it is a resultset
            }
            metaData = rs.getMetaData();
            columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                jasperReport.addColumn(col.column(metaData.getColumnLabel(i), metaData.getColumnLabel(i), type.stringType()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }

}
