package org.bahmni.reports.dao.impl;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.model.GenericLabOrderReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.util.SqlUtil;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import static org.bahmni.reports.util.GenericLabOrderReportTemplateHelper.*;
import static org.bahmni.reports.util.GenericReportsHelper.constructExtraPatientIdentifiersToFilter;

public class GenericLabOrderDaoImpl implements GenericDao {

    private BahmniReportsProperties bahmniReportsProperties;
    private Report<GenericLabOrderReportConfig> report;

    public GenericLabOrderDaoImpl(Report<GenericLabOrderReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
        this.report = report;
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public ResultSet getResultSet(Connection connection,
                                  String startDate, String endDate, List<String> conceptNamesToFilter)
            throws SQLException, InvalidConfigurationException {
        String sql = getFileContent("sql/genericLabOrderReport.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (report.getConfig() != null) {
            sqlTemplate.add("patientAttributes", constructPatientAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("patientAddresses", constructPatientAddressesToDisplay(report.getConfig()));
            sqlTemplate.add("visitAttributes", constructVisitAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("programsToFilter", constructProgramsString(report.getConfig()));
            sqlTemplate.add("conceptNamesToFilter", constructConceptNamesToFilter(report, bahmniReportsProperties));
            sqlTemplate.add("showProvider", report.getConfig().showProvider());
            String conceptValuesToFilter = conceptValuesToFilter(report.getConfig());
            if (conceptValuesToFilter.isEmpty()) {
                sqlTemplate.add("noValueFilter", "NULL");
            }
            sqlTemplate.add("conceptValuesToFilter", conceptValuesToFilter);
            sqlTemplate.add("numericRangesFilterSql", constructNumericRangeFilters(report.getConfig()));
            sqlTemplate.add("extraPatientIdentifierTypes", constructExtraPatientIdentifiersToFilter(report.getConfig()));
            sqlTemplate.add("ageGroupName", report.getConfig().getAgeGroupName());
            sqlTemplate.add("showReferredOutTests", report.getConfig().showReferredOutTests());
            if(report.getConfig().getSortBy() != null && report.getConfig().getSortBy().size() > 0) {
                sqlTemplate.add("sortByColumns", constructSortByColumnsOrder(report.getConfig()));
            }
        }

        return SqlUtil.executeSqlWithStoredProc(connection, sqlTemplate.render());
    }
}
