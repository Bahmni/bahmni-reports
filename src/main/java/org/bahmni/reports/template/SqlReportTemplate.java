package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SqlReportConfig;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

//import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

public class SqlReportTemplate extends BaseReportTemplate<SqlReportConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<SqlReportConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
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
                jasperReport.addColumn(col.column(metaData.getColumnLabel(i), metaData.getColumnLabel(i), mapSqlDataTypeToJasperDataType(metaData.getColumnType(i))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        jasperReport.setDataSource(resultSet);
        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);
        resources.add(statement);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getSqlString(Report<SqlReportConfig> reportConfig, String startDate, String endDate) {
        String sql = getFileContent(reportConfig.getConfig().getSqlPath(), true);
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }

    private DRIDataType mapSqlDataTypeToJasperDataType(int sqlType){

        switch(sqlType){
            case Types.BIT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return DataTypes.integerType();

            case Types.BIGINT:
                return DataTypes.longType();

            case Types.FLOAT:
                return DataTypes.floatType();
            case Types.DOUBLE:
                return DataTypes.doubleType();
            case Types.DATE:
                return DataTypes.dateType();
            default:
                return DataTypes.stringType();
        }
    }

}
