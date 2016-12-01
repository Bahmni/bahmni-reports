package org.bahmni.reports.dao.impl;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.model.GenericObservationFormReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.util.GenericObservationFormReportTemplateHelper;
import org.bahmni.reports.util.SqlUtil;
import org.bahmni.webclients.WebClientsException;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.constructConceptNameSelectSql;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.constructObsFormConceptIdsToFilter;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.constructProgramAttributesSql;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.constructProgramAttributesString;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.fetchConceptIds;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.conceptValuesToFilter;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructConceptClassesToFilter;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructConceptNamesToFilter;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructLocationTagsToFilter;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructNumericRangeFilters;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructProgramsString;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructVisitTypesString;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getConceptNameFormatSql;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getDateRangeFor;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getListOfFullySpecifiedNames;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getVisitTypesToFilter;
import static org.bahmni.reports.util.GenericReportsHelper.constructExtraPatientIdentifiersToFilter;
import static org.bahmni.reports.util.GenericReportsHelper.constructPatientAddressesToDisplay;
import static org.bahmni.reports.util.GenericReportsHelper.constructPatientAttributeNamesToDisplay;
import static org.bahmni.reports.util.GenericReportsHelper.constructSortByColumnsOrder;
import static org.bahmni.reports.util.GenericReportsHelper.constructVisitAttributeNamesToDisplay;

public class GenericObservationFormDaoImpl implements GenericDao {

    private BahmniReportsProperties bahmniReportsProperties;
    private Report<GenericObservationFormReportConfig> report;

    public GenericObservationFormDaoImpl(Report<GenericObservationFormReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
        this.report = report;
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public ResultSet getResultSet(Connection connection,
                                  String startDate, String endDate, List<String> formNamesToFilter)
            throws SQLException, WebClientsException, InvalidConfigurationException {
        String sql = getFileContent("sql/genericObservationFormReport.sql");
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
            List<String> conceptNames =  getListOfFullySpecifiedNames(GenericObservationFormReportTemplateHelper.fetchLeafConceptsAsList(report, bahmniReportsProperties))  ;
            List<Integer> conceptIds =  fetchConceptIds(report, bahmniReportsProperties)  ;
            sqlTemplate.add("formNamesToFilter", constructConceptNamesToFilter(conceptNames));
            sqlTemplate.add("obsFormIdToFilter", constructObsFormConceptIdsToFilter(conceptIds));
            String conceptValuesToFilter = conceptValuesToFilter(report.getConfig());
            if (conceptValuesToFilter.isEmpty()){
                sqlTemplate.add("noValueFilter", "NULL");
            }

            if (conceptValuesToFilter.contains("\"\"")){
                sqlTemplate.add("nullIncludedFilter", "NULL");
            }

            sqlTemplate.add("conceptValuesToFilter", conceptValuesToFilter);
            sqlTemplate.add("numericRangesFilterSql", constructNumericRangeFilters(report.getConfig()));
            sqlTemplate.add("selectConceptNamesSql", constructConceptNameSelectSql(formNamesToFilter));
            sqlTemplate.add("selectProgramAttributesSql", constructProgramAttributesSql(report.getConfig()));
            sqlTemplate.add("showProvider", report.getConfig().showProvider());
            sqlTemplate.add("visitTypesToFilter", constructVisitTypesString(getVisitTypesToFilter(report.getConfig())));
            sqlTemplate.add("extraPatientIdentifierTypes", constructExtraPatientIdentifiersToFilter(report.getConfig()));
            sqlTemplate.add("ageGroupName", report.getConfig().getAgeGroupName());
            if(report.getConfig().isIgnoreEmptyValues()) {
                sqlTemplate.add("ignoreEmptyValues", "Having Value is not null");
            }
            if(report.getConfig().getSortBy() != null && report.getConfig().getSortBy().size() > 0) {
                 sqlTemplate.add("sortByColumns", constructSortByColumnsOrder(report.getConfig()));
            }
        }
        sqlTemplate.add("concept_name_sql", getConceptNameFormatSql(report.getConfig()));
        sqlTemplate.add("applyDateRangeFor", getDateRangeFor(report.getConfig()));

        return SqlUtil.executeSqlWithStoredProc(connection, sqlTemplate.render());
    }

}
