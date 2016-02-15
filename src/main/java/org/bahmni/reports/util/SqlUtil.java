package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SqlUtil {
    public static String toEscapedCommaSeparatedSqlString(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            sb.append("\\'").append(StringEscapeUtils.escapeSql(item)).append("\\',");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String toCommaSeparatedSqlString(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            sb.append("'").append(StringEscapeUtils.escapeSql(item)).append("',");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static JasperReportBuilder executeReportWithStoredProc(JasperReportBuilder jasperReport, Connection connection, String formattedSql) {
        Statement stmt;

        try {
            stmt = connection.createStatement();
            boolean hasMoreResultSets = stmt.execute(formattedSql);
            while (hasMoreResultSets ||
                    stmt.getUpdateCount() != -1) { //if there are any more queries to be processed
                if (hasMoreResultSets) {
                    ResultSet rs = stmt.getResultSet();
                    if (rs.isBeforeFirst()) {
                        jasperReport.setDataSource(rs);
                        return jasperReport;
                    }
                }
                hasMoreResultSets = stmt.getMoreResults(); //true if it is a resultset
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jasperReport;
    }
}
