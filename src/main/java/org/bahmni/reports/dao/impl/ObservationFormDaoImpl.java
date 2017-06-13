package org.bahmni.reports.dao.impl;

import org.bahmni.reports.dao.ObservationFormDao;
import org.bahmni.reports.model.GenericObservationFormReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.util.SqlUtil;
import org.bahmni.webclients.WebClientsException;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.*;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.*;
import static org.bahmni.reports.util.GenericReportsHelper.constructExtraPatientIdentifiersToFilter;
import static org.bahmni.reports.util.GenericReportsHelper.constructPatientAddressesToDisplay;
import static org.bahmni.reports.util.GenericReportsHelper.constructPatientAttributeNamesToDisplay;
import static org.bahmni.reports.util.GenericReportsHelper.constructSortByColumnsOrder;
import static org.bahmni.reports.util.GenericReportsHelper.constructVisitAttributeNamesToDisplay;

public class ObservationFormDaoImpl implements ObservationFormDao {

    private Report<GenericObservationFormReportConfig> report;

    public ObservationFormDaoImpl(Report<GenericObservationFormReportConfig> report) {
        this.report = report;
    }

    @Override
    public ResultSet getResultSet(Connection connection,
                                  String startDate, String endDate, List<String> conceptNamesToFilter, List<String> formVersionsList)
            throws SQLException, WebClientsException, InvalidConfigurationException {
        String sql = getFileContent("sql/observationFormReport.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (report.getConfig() != null) {
            sqlTemplate.add("patientAttributes", constructPatientAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("patientAddresses", constructPatientAddressesToDisplay(report.getConfig()));
            sqlTemplate.add("visitAttributes", constructVisitAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("locationTagsToFilter", constructLocationTagsToFilter(report.getConfig()));
            sqlTemplate.add("conceptClassesToFilter", constructConceptClassesToFilter(report.getConfig()));
            sqlTemplate.add("programsToFilter", constructProgramsString(report.getConfig()));
            sqlTemplate.add("programsAttributeTypesToFilter", constructProgramAttributesString(report.getConfig()));
            sqlTemplate.add("formNamesToFilter", constructRegexForFormNameAndVersion(formVersionsList));
            String conceptValuesToFilter = conceptValuesToFilter(report.getConfig());
            if (conceptValuesToFilter.isEmpty()){
                sqlTemplate.add("noValueFilter", "NULL");
            }

            if (conceptValuesToFilter.contains("\"\"")){
                sqlTemplate.add("nullIncludedFilter", "NULL");
            }

            sqlTemplate.add("conceptValuesToFilter", conceptValuesToFilter);
            sqlTemplate.add("numericRangesFilterSql", constructNumericRangeFilters(report.getConfig()));
            sqlTemplate.add("selectConceptNamesSql", constructConceptNameSelectSql(conceptNamesToFilter));
            sqlTemplate.add("selectProgramAttributesSql", constructProgramAttributesSql(report.getConfig()));
            sqlTemplate.add("showProvider", report.getConfig().showProvider());
            sqlTemplate.add("visitTypesToFilter", constructVisitTypesString(getVisitTypesToFilter(report.getConfig())));
            sqlTemplate.add("extraPatientIdentifierTypes", constructExtraPatientIdentifiersToFilter(report.getConfig()));
            sqlTemplate.add("ageGroupName", report.getConfig().getAgeGroupName());
            if(report.getConfig().getSortBy() != null && report.getConfig().getSortBy().size() > 0) {
                 sqlTemplate.add("sortByColumns", constructSortByColumnsOrder(report.getConfig()));
            }
        }
        sqlTemplate.add("concept_name_sql", getConceptNameFormatSql(report.getConfig()));
        sqlTemplate.add("applyDateRangeFor", getDateRangeFor(report.getConfig()));

        return SqlUtil.executeSqlWithStoredProc(connection, sqlTemplate.render());
    }

}
