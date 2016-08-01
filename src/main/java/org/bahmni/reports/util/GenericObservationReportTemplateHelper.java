package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.webclients.HttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericObservationReportTemplateHelper {

    public static void createAndAddPatientAttributeColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        for (String patientAttribute : getPatientAttributes(config)) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static void createAndAddVisitAttributeColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        for (String visitAttribute : getVisitAttributes(config)) {
            TextColumnBuilder<String> column = col.column(visitAttribute, visitAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static void createAndAddProviderNameColumn(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        if (config.showProvider()) {
            TextColumnBuilder<String> providerColumn = col.column("Provider", "Provider", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(providerColumn);
        }
    }

    public static void createAndAddPatientAddressColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        for (String address : getPatientAddresses(config)) {
            TextColumnBuilder<String> column = col.column(address, address, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static String constructPatientAttributeNamesToDisplay(GenericObservationReportConfig config) {
        List<String> patientAttributes = getPatientAttributes(config);
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pat.name = \\'%s\\', IF(pat.format = \\'org.openmrs.Concept\\',coalesce(scn.name, fscn.name),pa.value), NULL))) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute.replace("'", "\\\\\\\'"), patientAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");
    }

    private static List<String> getPatientAttributes(GenericObservationReportConfig config) {
        return config.getPatientAttributes() != null ? config.getPatientAttributes() : new ArrayList<String>();
    }

    private static List<String> getVisitAttributes(GenericObservationReportConfig config) {
        return config.getVisitAttributes() != null ? config.getVisitAttributes() : new ArrayList<String>();
    }

    private static List<String> getPatientAddresses(GenericObservationReportConfig config) {
        return config.getPatientAddresses() != null ? config.getPatientAddresses() : new ArrayList<String>();
    }

    private static List<String> getLocationTagsToFilter(GenericObservationReportConfig config) {
        return config.getLocationTagsToFilter() != null ? config.getLocationTagsToFilter() : new ArrayList<String>();
    }

    private static List<String> getProgramsToFilter(GenericObservationReportConfig config) {
        return config.getProgramsToFilter() != null ? config.getProgramsToFilter() : new ArrayList<String>();
    }

    public static String constructPatientAddressesToDisplay(GenericObservationReportConfig config) {
        List<String> patientAddresses = getPatientAddresses(config);
        StringBuilder stringBuilder = new StringBuilder();
        if (patientAddresses != null) {
            for (String address : patientAddresses) {
                stringBuilder.append("paddress").append(".").append(address).append(", ");
            }
        }
        return stringBuilder.toString();
    }

    public static String constructVisitAttributeNamesToDisplay(GenericObservationReportConfig config) {
        List<String> visitAttributes = getVisitAttributes(config);
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(vat.name = \\'%s\\', va.value_reference, NULL))) AS \\'%s\\'";

        for (String visitAttribute : visitAttributes) {
            parts.add(String.format(helperString, visitAttribute.replace("'", "\\\\\\\'"), visitAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");

    }

    public static String constructLocationTagsToFilter(GenericObservationReportConfig config) {
        List<String> locationTagsToFilter = getLocationTagsToFilter(config);
        List<String> parts = new ArrayList<>();
        for (String locationTag : locationTagsToFilter) {
            parts.add("\"" + locationTag + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    public static String constructProgramsString(GenericObservationReportConfig config) {
        List<String> programsToFilter = getProgramsToFilter(config);
        List<String> parts = new ArrayList<>();
        for (String programName : programsToFilter) {
            parts.add("\"" + programName.replace("'", "\\\\\\\'") + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    public static void createAndAddDefaultColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        TextColumnBuilder<String> patientIdentifierColumn = col.column("Patient Identifier", "Patient Identifier", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age", "Age", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> birthdateColumn = col.column("Birthdate", "Birthdate", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> locationNameColumn = col.column("Location Name", "Location Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> programNameColumn = col.column("Program Name", "Program Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> programEnrollmentDateColumn = col.column("Program Enrollment Date", "Program Enrollment Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> programEndDateColumn = col.column("Program End Date", "Program End Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientCreatedDateColumn = col.column("Patient Created Date", "Patient Created Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, locationNameColumn, programNameColumn, programEnrollmentDateColumn, programEndDateColumn, patientCreatedDateColumn);

        if (config == null || !config.isEncounterPerRow()) {
            TextColumnBuilder<String> conceptNameColumn = col.column("Concept Name", "Concept Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> valueColumn = col.column("Value", "Value", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> parentConcept = col.column("Parent Concept", "Parent Concept", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> obsDatetime = col.column("Observation Datetime", "Observation Datetime", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> obsDate = col.column("Observation Date", "Observation Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.columns(conceptNameColumn, valueColumn, obsDatetime, obsDate, parentConcept);
        }
    }

    public static void createAndAddVisitInfoColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        if (config.showVisitInfo()) {
            TextColumnBuilder<String> visitTypeColumn = col.column("Visit Type", "Visit Type", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> visitStartDateColumn = col.column("Visit Start Date", "Visit Start Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> visitStopDateColumn = col.column("Visit Stop Date", "Visit Stop Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.columns(visitTypeColumn, visitStartDateColumn, visitStopDateColumn);
        }
    }

    public static void createAndAddConceptColumns(List<String> conceptNames, JasperReportBuilder jasperReport) {
        for (String conceptName : conceptNames) {
            TextColumnBuilder<String> column = col.column(conceptName, conceptName, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
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

    public static List<String> fetchLeafConceptsAsList(Report<GenericObservationReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
        List<String> conceptNamesToFilter = getConceptNamesToFilter(report.getConfig());
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

    private static List<String> fetchChildConceptsAsList(List<String> conceptNamesToFilter, Report<GenericObservationReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
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

    private static String getConceptNamesParameter(List<String> conceptNamesToFilter) {
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

    public static String constructConceptNameSelectSqlIfShowInOneRow(List<String> conceptNamesToFilter, GenericObservationReportConfig config) {
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        if (config.isEncounterPerRow()) {
            String helperString = "GROUP_CONCAT(DISTINCT(IF(obs_fscn.name = \\'%s\\', coalesce(o.value_numeric, o.value_boolean, o.value_text, o.value_datetime, coded_scn.name, coded_fscn.name), NULL)) ORDER BY o.obs_id DESC) AS \\'%s\\'";
            for (String conceptName : conceptNamesToFilter) {
                conceptNamesWithDoubleQuote.add(String.format(helperString, conceptName.replace("'", "\\\\\\\'"), conceptName.replace("'", "\\\\\\\'")));
            }
        }
        return StringUtils.join(conceptNamesWithDoubleQuote, ',');
    }

    public static String constructConceptNamesToFilter(Report<GenericObservationReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
        List<String> conceptNamesToFilter = getConceptNamesToFilter(report.getConfig());
        List<String> childConceptsAsList = fetchChildConceptsAsList(conceptNamesToFilter, report, bahmniReportsProperties);
        if (CollectionUtils.isEmpty(childConceptsAsList)) {
            return null;
        }
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        for (String conceptName : childConceptsAsList) {
            conceptNamesWithDoubleQuote.add("\"" + conceptName + "\"");
        }
        return StringUtils.join(conceptNamesWithDoubleQuote, ',');
    }

    public static String constructConceptClassesToFilter(GenericObservationReportConfig config) {
        List<String> conceptClassesToFilter = getConceptClassesToFilter(config);
        List<String> conceptClassesWithDoubleQuote = new ArrayList<>();
        for (String conceptClass : conceptClassesToFilter) {
            conceptClassesWithDoubleQuote.add("\"" + conceptClass + "\"");
        }
        return StringUtils.join(conceptClassesWithDoubleQuote, ',');
    }

    public static String getDateRangeFor(GenericObservationReportConfig config) {
        if (config != null) {
            return config.getApplyDateRangeFor();
        }
        return "obsDateTime";
    }

    private static List<String> getConceptNamesToFilter(GenericObservationReportConfig config) {
        return config.getConceptNamesToFilter() != null ? config.getConceptNamesToFilter() : new ArrayList<String>();
    }

    private static List<String> getConceptClassesToFilter(GenericObservationReportConfig config) {
        return config.getConceptClassesToFilter() != null ? config.getConceptClassesToFilter() : new ArrayList<String>();
    }

    public static List<String> getVisitTypesToFilter(GenericObservationReportConfig config) {
        return config.getVisitTypesToFilter() != null ? config.getVisitTypesToFilter() : new ArrayList<String>();
    }

    public static String constructVisitTypesString(List<String> visitTypesToFilter) {
        List<String> parts = new ArrayList<>();
        for (String visitType : visitTypesToFilter) {
            parts.add("\"" + visitType + "\"");
        }
        return StringUtils.join(parts, ',');
    }

}
