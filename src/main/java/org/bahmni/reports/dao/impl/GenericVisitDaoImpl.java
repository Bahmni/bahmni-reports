package org.bahmni.reports.dao.impl;

import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.model.GenericVisitReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.util.SqlUtil;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import static org.bahmni.reports.util.GenericVisitReportTemplateHelper.*;

public class GenericVisitDaoImpl implements GenericDao {

    private Report<GenericVisitReportConfig> report;

    public GenericVisitDaoImpl(Report<GenericVisitReportConfig> report) {
        this.report = report;
    }

    @Override
    public ResultSet getResultSet(Connection connection, String startDate, String endDate, List<String> conceptNamesToFilter) throws SQLException, InvalidConfigurationException {
        String sql = getFileContent("sql/genericVisitReport.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (report.getConfig() != null) {
            sqlTemplate.add("patientAttributes", constructPatientAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("patientAddresses", constructPatientAddressesToDisplay(report.getConfig()));
            sqlTemplate.add("visitAttributes", constructVisitAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("visitTypesToFilter", constructVisitTypesString(getVisitTypesToFilter(report.getConfig())));
            sqlTemplate.add("extraPatientIdentifierTypes", constructExtraPatientIdentifiersToFilter(report.getConfig()));
            sqlTemplate.add("ageGroupName", report.getConfig().getAgeGroupName());
            if(report.getConfig().getSortBy() != null && report.getConfig().getSortBy().size() > 0) {
                sqlTemplate.add("sortByColumns", constructSortByColumnsOrder(report.getConfig()));
            }
        }
        sqlTemplate.add("applyDateRangeFor", getDateRangeFor(report.getConfig()));
        return SqlUtil.executeSqlWithStoredProc(connection, sqlTemplate.render());
    }

}
