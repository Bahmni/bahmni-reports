package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.*;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.bahmni.webclients.HttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.stringtemplate.v4.ST;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class GenericObservationReportTemplate extends BaseReportTemplate<GenericObservationReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public GenericObservationReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<GenericObservationReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);

        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        createAndAddMandatoryColumns(jasperReport, report.getConfig());
        if (report.getConfig() != null) {
            createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
            createAndAddPatientAddressColumns(jasperReport, report.getConfig());
            createAndAddVisitInfoColumns(jasperReport, report.getConfig());
            createAndAddVisitAttributeColumns(jasperReport, report.getConfig());
            createAndAddProviderNameColumn(jasperReport, report.getConfig());
            if (report.getConfig().isEncounterPerRow()) {
                List<String> conceptNames = fetchLeafConceptsAsList(getConceptNamesToFilter(report.getConfig()), report);
                createAndAddConceptColumns(conceptNames, jasperReport);
            }
            createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
        }

        String formattedSql = getFormattedSql(report, startDate, endDate);
        return SqlUtil.executeReportWithStoredProc(jasperReport, connection, formattedSql);
    }

    private void createAndAddVisitInfoColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        if (config.showVisitInfo()) {
            TextColumnBuilder<String> visitTypeColumn = col.column("Visit Type", "Visit Type", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> visitStartDateColumn = col.column("Visit Start Date", "Visit Start Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> visitStopDateColumn = col.column("Visit Stop Date", "Visit Stop Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.columns(visitTypeColumn, visitStartDateColumn, visitStopDateColumn);
        }
    }

    private void createAndAddProviderNameColumn(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        if (config.showProvider()) {
            TextColumnBuilder<String> providerColumn = col.column("Provider", "Provider", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(providerColumn);
        }
    }

    private String getFormattedSql(Report<GenericObservationReportConfig> report, String startDate, String endDate) {
        String sql = "";
        if (report.getConfig() != null && report.getConfig().isEncounterPerRow()) {
            sql = getFileContent("sql/genericObservationReportInOneRow.sql");
        } else {
            sql = getFileContent("sql/genericObservationReport.sql");
        }
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (report.getConfig() != null) {
            sqlTemplate.add("patientAttributes", constructPatientAttributeNamesString(getPatientAttributes(report.getConfig())));
            sqlTemplate.add("patientAddresses", constructPatientAddresses(getPatientAddresses(report.getConfig())));
            sqlTemplate.add("visitAttributes", constructVisitAttributeNamesString(getVisitAttributes(report.getConfig())));
            sqlTemplate.add("locationTagsToFilter", constructLocationTagsString(getLocationTagsToFilter(report.getConfig())));
            sqlTemplate.add("conceptClassesToFilter", constructConceptClassesString(getConceptClassesToFilter(report.getConfig())));
            sqlTemplate.add("programsToFilter", constructProgramsString(getProgramsToFilter(report.getConfig())));
            sqlTemplate.add("conceptNamesToFilter", constructConceptNamesString(fetchChildConceptsAsList(getConceptNamesToFilter(report.getConfig()), report)));
            sqlTemplate.add("selectConceptNamesSql", constructConceptNameSelectSqlIfShowInOneRow(fetchLeafConceptsAsList(getConceptNamesToFilter(report.getConfig()), report), report.getConfig()));
            sqlTemplate.add("showProvider", report.getConfig().showProvider());
        }
        sqlTemplate.add("applyDateRangeFor", getDateRangeFor(report.getConfig()));
        return sqlTemplate.render();
    }

    private List<String> getConceptNamesToFilter(GenericObservationReportConfig config) {
        return config.getConceptNamesToFilter() != null ? config.getConceptNamesToFilter() : new ArrayList<String>();
    }

    private List<String> getProgramsToFilter(GenericObservationReportConfig config) {
        return config.getProgramsToFilter() != null ? config.getProgramsToFilter() : new ArrayList<String>();
    }

    private void createAndAddVisitAttributeColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        for (String visitAttribute : getVisitAttributes(config)) {
            TextColumnBuilder<String> column = col.column(visitAttribute, visitAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private void createAndAddPatientAddressColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        for (String address : getPatientAddresses(config)) {
            TextColumnBuilder<String> column = col.column(address, address, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private void createAndAddPatientAttributeColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        for (String patientAttribute : getPatientAttributes(config)) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private void createAndAddConceptColumns(List<String> conceptNames, JasperReportBuilder jasperReport) {
        for (String conceptName : conceptNames) {
            TextColumnBuilder<String> column = col.column(conceptName, conceptName, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        if (config.isForDataAnalysis()) {
            TextColumnBuilder<Long> patientId = col.column("Patient Id", "Patient Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Long> encounterId = col.column("Encounter Id", "Encounter Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Long> visitId = col.column("Visit Id", "Visit Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            if (config.isEncounterPerRow()) {
                jasperReport.columns(patientId, visitId, encounterId);
            } else {
                TextColumnBuilder<Long> conceptId = col.column("Concept Id", "Concept Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
                TextColumnBuilder<String> conceptShortName = col.column("Concept Short Name", "Concept Short Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
                TextColumnBuilder<String> conceptFullName = col.column("Concept Full Name", "Concept Full Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
                TextColumnBuilder<Long> obsId = col.column("Obs Id", "Obs Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
                TextColumnBuilder<Long> obsGroupId = col.column("Obs Group Id", "Obs Group Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
                TextColumnBuilder<Long> orderId = col.column("Order Id", "Order Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
                jasperReport.columns(patientId, visitId, encounterId, obsId, obsGroupId, orderId, conceptId, conceptFullName, conceptShortName);
            }
        }
    }

    private void createAndAddMandatoryColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        TextColumnBuilder<String> patientIdentifierColumn = col.column("Patient Identifier", "Patient Identifier", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age", "Age", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> birthdateColumn = col.column("Birthdate", "Birthdate", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> locationNameColumn = col.column("Location Name", "Location Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, locationNameColumn);
        if (config != null && config.isEncounterPerRow()) {
            return;
        }
        TextColumnBuilder<String> conceptNameColumn = col.column("Concept Name", "Concept Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> valueColumn = col.column("Value", "Value", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> parentConcept = col.column("Parent Concept", "Parent Concept", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> obsDatetime = col.column("Observation Datetime", "Observation Datetime", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.columns(conceptNameColumn, valueColumn, obsDatetime, parentConcept);
    }

    private List<String> fetchLeafConceptsAsList(List<String> conceptNamesToFilter, Report<GenericObservationReportConfig> report) {
        if (CollectionUtils.isEmpty(conceptNamesToFilter) || !report.getConfig().isEncounterPerRow()) {
            return new ArrayList<>();
        }
        HttpClient httpClient = report.getHttpClient();
        try {
            String url = bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/leafConceptNames?" + getConceptNamesParameter(conceptNamesToFilter);
            String response = httpClient.get(new URI(url));
            return new ObjectMapper().readValue(response, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String> fetchChildConceptsAsList(List<String> conceptNamesToFilter, Report<GenericObservationReportConfig> report) {
        if (CollectionUtils.isEmpty(conceptNamesToFilter) || report.getConfig().isEncounterPerRow()) {
            return new ArrayList<>();
        }
        HttpClient httpClient = report.getHttpClient();
        try {
            String url = bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?" + getConceptNamesParameter(conceptNamesToFilter);
            String response = httpClient.get(new URI(url));
            return new ObjectMapper().readValue(response, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getConceptNamesParameter(List<String> conceptNamesToFilter) {
        List<String> parameters = new ArrayList<>();
        for (String conceptName : conceptNamesToFilter) {
            try {
                String encodedTemplateName = URLEncoder.encode(conceptName, "UTF-8");
                parameters.add("conceptNames=" + encodedTemplateName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return StringUtils.join(parameters, '&');
    }


    private String constructConceptClassesString(List<String> conceptClassesToFilter) {
        List<String> conceptClassesWithDoubleQuote = new ArrayList<>();
        for (String conceptClass : conceptClassesToFilter) {
            conceptClassesWithDoubleQuote.add("\"" + conceptClass + "\"");
        }
        return StringUtils.join(conceptClassesWithDoubleQuote, ',');
    }

    private String constructLocationTagsString(List<String> locationTagsToFilter) {
        List<String> parts = new ArrayList<>();
        for (String locationTag : locationTagsToFilter) {
            parts.add("\"" + locationTag + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    private String constructPatientAddresses(List<String> patientAddresses) {
        StringBuilder stringBuilder = new StringBuilder();
        if (patientAddresses != null) {
            for (String address : patientAddresses) {
                stringBuilder.append("paddress").append(".").append(address).append(", ");
            }
        }
        return stringBuilder.toString();
    }

    private String constructVisitAttributeNamesString(List<String> visitAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(vat.name = \\'%s\\', va.value_reference, NULL))) AS \\'%s\\'";

        for (String visitAttribute : visitAttributes) {
            parts.add(String.format(helperString, visitAttribute.replace("'", "\\\\\\\'"), visitAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");
    }

    private String constructPatientAttributeNamesString(List<String> patientAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pat.name = \\'%s\\', IF(pat.format = \\'org.openmrs.Concept\\',coalesce(scn.name, fscn.name),pa.value), NULL))) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute.replace("'", "\\\\\\\'"), patientAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");
    }

    private String constructConceptNameSelectSqlIfShowInOneRow(List<String> conceptNamesToFilter, GenericObservationReportConfig config) {
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        if (config.isEncounterPerRow()) {
            String helperString = "GROUP_CONCAT(DISTINCT(IF(obs_fscn.name = \\'%s\\', coalesce(o.value_numeric, o.value_boolean, o.value_text, o.value_datetime, coded_scn.name, coded_fscn.name), NULL)) ORDER BY o.obs_id DESC) AS \\'%s\\'";
            for (String conceptName : conceptNamesToFilter) {
                conceptNamesWithDoubleQuote.add(String.format(helperString, conceptName.replace("'", "\\\\\\\'"), conceptName.replace("'", "\\\\\\\'")));
            }
        }
        return StringUtils.join(conceptNamesWithDoubleQuote, ',');
    }

    private String constructConceptNamesString(List<String> conceptNamesToFilter) {
        if (CollectionUtils.isEmpty(conceptNamesToFilter)) {
            return null;
        }
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        for (String conceptName : conceptNamesToFilter) {
            conceptNamesWithDoubleQuote.add("\"" + conceptName + "\"");
        }
        return StringUtils.join(conceptNamesWithDoubleQuote, ',');
    }

    private String constructProgramsString(List<String> programsToFilter) {
        List<String> parts = new ArrayList<>();
        for (String programName : programsToFilter) {
            parts.add("\"" + programName.replace("'", "\\\\\\\'") + "\"");
        }
        return StringUtils.join(parts, ',');

    }

    private List<String> getPatientAttributes(GenericObservationReportConfig config) {
        return config.getPatientAttributes() != null ? config.getPatientAttributes() : new ArrayList<String>();
    }

    private List<String> getVisitAttributes(GenericObservationReportConfig config) {
        return config.getVisitAttributes() != null ? config.getVisitAttributes() : new ArrayList<String>();
    }

    private List<String> getPatientAddresses(GenericObservationReportConfig config) {
        return config.getPatientAddresses() != null ? config.getPatientAddresses() : new ArrayList<String>();
    }

    private List<String> getLocationTagsToFilter(GenericObservationReportConfig config) {
        return config.getLocationTagsToFilter() != null ? config.getLocationTagsToFilter() : new ArrayList<String>();
    }

    private List<String> getConceptClassesToFilter(GenericObservationReportConfig config) {
        return config.getConceptClassesToFilter() != null ? config.getConceptClassesToFilter() : new ArrayList<String>();
    }

    private String getDateRangeFor(GenericObservationReportConfig config) {
        if (config != null) {
            return config.getApplyDateRangeFor();
        }
        return "obsDateTime";
    }
}
