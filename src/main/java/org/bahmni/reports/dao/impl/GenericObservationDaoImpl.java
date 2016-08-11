package org.bahmni.reports.dao.impl;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructConceptClassesToFilter;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructConceptNameSelectSqlIfShowInOneRow;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructConceptNamesToFilter;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructLocationTagsToFilter;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructPatientAddressesToDisplay;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructProgramsString;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.constructVisitTypesString;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getConceptNameFormatSql;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getDateRangeFor;
import static org.bahmni.reports.util.GenericReportsHelper.constructExtraPatientIdentifiersToFilter;
import static org.bahmni.reports.util.GenericReportsHelper.constructPatientAttributeNamesToDisplay;
import static org.bahmni.reports.util.GenericReportsHelper.constructVisitAttributeNamesToDisplay;

public class GenericObservationDaoImpl implements GenericDao {

    private BahmniReportsProperties bahmniReportsProperties;
    private Report<GenericObservationReportConfig> report;

    public GenericObservationDaoImpl(Report<GenericObservationReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
        this.report = report;
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public ResultSet getResultSet(Connection connection, String startDate, String endDate, List<String> conceptNamesToFilter) throws SQLException {
        String sql;
        GenericObservationReportConfig reportConfig = report.getConfig();
        if (reportConfig != null && reportConfig.isEncounterPerRow()) {
            sql = getFileContent("sql/genericObservationReportInOneRow.sql");
        } else {
            sql = getFileContent("sql/genericObservationReport.sql");
        }
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (reportConfig != null) {
            fillConfigBasedPlaceHolders(conceptNamesToFilter, sqlTemplate);
        }
        sqlTemplate.add("concept_name_sql", getConceptNameFormatSql(reportConfig));
        sqlTemplate.add("applyDateRangeFor", getDateRangeFor(reportConfig));
        return SqlUtil.executeSqlWithStoredProc(connection, sqlTemplate.render());
    }

    private void fillConfigBasedPlaceHolders(List<String> conceptNamesToFilter, ST sqlTemplate) {
        GenericObservationReportConfig reportConfig = report.getConfig();
        sqlTemplate.add("patientAttributes", constructPatientAttributeNamesToDisplay(reportConfig));
        sqlTemplate.add("patientAddresses", constructPatientAddressesToDisplay(reportConfig));
        sqlTemplate.add("visitAttributes", constructVisitAttributeNamesToDisplay(reportConfig));
        sqlTemplate.add("locationTagsToFilter", constructLocationTagsToFilter(reportConfig));
        sqlTemplate.add("conceptClassesToFilter", constructConceptClassesToFilter(reportConfig));
        sqlTemplate.add("programsToFilter", constructProgramsString(reportConfig));
        sqlTemplate.add("conceptNamesToFilter", constructConceptNamesToFilter(report, bahmniReportsProperties));
        sqlTemplate.add("selectConceptNamesSql", constructConceptNameSelectSqlIfShowInOneRow(conceptNamesToFilter, reportConfig));
        sqlTemplate.add("showProvider", reportConfig.showProvider());
        sqlTemplate.add("visitTypesToFilter", constructVisitTypesString(reportConfig.getVisitTypesToFilter()));
        sqlTemplate.add("extraPatientIdentifierTypes", constructExtraPatientIdentifiersToFilter(reportConfig));
        sqlTemplate.add("ageGroupName", reportConfig.getAgeGroupName());
    }

}
