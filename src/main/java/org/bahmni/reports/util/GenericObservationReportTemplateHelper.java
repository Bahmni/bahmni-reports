package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ConceptName;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.WebClientsException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericObservationReportTemplateHelper extends GenericReportsHelper {

    public static void createAndAddProviderNameColumn(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        if (config.showProvider()) {
            TextColumnBuilder<String> providerColumn = col.column("Provider", "Provider", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(providerColumn);
        }
    }

    private static List<String> getLocationTagsToFilter(GenericObservationReportConfig config) {
        return config.getLocationTagsToFilter() != null ? config.getLocationTagsToFilter() : new ArrayList<String>();
    }

    private static List<String> getProgramsToFilter(GenericObservationReportConfig config) {
        return config.getProgramsToFilter() != null ? config.getProgramsToFilter() : new ArrayList<String>();
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
            TextColumnBuilder<String> obsCreatedDate = col.column("Observation Created Date", "Observation Created Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.columns(conceptNameColumn, valueColumn, obsDatetime, obsDate, obsCreatedDate, parentConcept);
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

    public static void createAndAddConceptColumns(List<ConceptName> conceptNames, JasperReportBuilder jasperReport, String conceptNameDisplayFormat) {
        for (ConceptName conceptName : conceptNames) {
            TextColumnBuilder<String> column = col.column(getConceptInDisplayFormat(conceptName, conceptNameDisplayFormat), conceptName.getFullySpecifiedName(), type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private static String getConceptInDisplayFormat(ConceptName conceptName, String conceptNameDisplayFormat) {
        if (conceptNameDisplayFormat == null) {
            return getDefaultConceptNameDisplayFormat(conceptName);
        }

        switch (conceptNameDisplayFormat) {
            case "shortNamePreferred":
                return preferShortConceptName(conceptName);
            case "fullySpecifiedName":
                return conceptName.getFullySpecifiedName();
            default:
                return getDefaultConceptNameDisplayFormat(conceptName);
        }
    }

    private static String preferShortConceptName(ConceptName conceptName) {
        return StringUtils.isEmpty(conceptName.getShortName()) ?
                conceptName.getFullySpecifiedName() : conceptName.getShortName();
    }

    private static String getDefaultConceptNameDisplayFormat(ConceptName conceptName) {
        if (conceptName.getShortName() == null) {
            return conceptName.getFullySpecifiedName();
        } else {
            return conceptName.getFullySpecifiedName() + "(" + conceptName.getShortName() + ")";
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
                TextColumnBuilder<Long> obsId = col.column("Obs Id", "Obs Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
                TextColumnBuilder<Long> obsGroupId = col.column("Obs Group Id", "Obs Group Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
                TextColumnBuilder<Long> orderId = col.column("Order Id", "Order Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
                jasperReport.columns(patientId, visitId, encounterId, obsId, obsGroupId, orderId, conceptId);
            }
        }
    }

    public static List<ConceptName> fetchLeafConceptsAsList(Report<GenericObservationReportConfig> report, BahmniReportsProperties bahmniReportsProperties) throws WebClientsException {
        List<String> conceptNamesToFilter = getConceptNamesToFilter(report.getConfig());
        if (CollectionUtils.isEmpty(conceptNamesToFilter)) {
            return new ArrayList<>();
        }
        return fetchConcepts(conceptNamesToFilter, report.getHttpClient(), bahmniReportsProperties);
    }

    public static List<ConceptName> fetchConcepts(List<String> conceptNamesToFilter, HttpClient httpClient, BahmniReportsProperties bahmniReportsProperties){
        try {
            String url = bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/leafConceptNames?" + getConceptNamesParameter(conceptNamesToFilter);
            String response = httpClient.get(new URI(url));
            return new ObjectMapper().readValue(response, new TypeReference<List<ConceptName>>() {
            });
        } catch (WebClientsException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


   public static List<String> fetchChildConceptsAsList(Report<GenericObservationReportConfig> report, BahmniReportsProperties bahmniReportsProperties) throws WebClientsException {
        List<String> conceptNamesToFilter = getConceptNamesToFilter(report.getConfig());
        if (CollectionUtils.isEmpty(conceptNamesToFilter) || report.getConfig().isEncounterPerRow()) {
            return new ArrayList<>();
        }
        HttpClient httpClient = report.getHttpClient();
        try {
            String url = bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?" + getConceptNamesParameter(conceptNamesToFilter);
            String response = httpClient.get(new URI(url));
            return new ObjectMapper().readValue(response, new TypeReference<List<String>>() {
            });
        } catch (WebClientsException e) {
                throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getConceptNamesParameter(List<String> conceptNamesToFilter) {
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

    public static List<String> getListOfFullySpecifiedNames(List<ConceptName> conceptNames) {
        List<String> fullySpecifiedNames = new ArrayList<>();
        for (ConceptName conceptName : conceptNames)
            fullySpecifiedNames.add(conceptName.getFullySpecifiedName());
        return fullySpecifiedNames;
    }

    public static String constructConceptNameSelectSqlIfShowInOneRow(List<String> conceptNamesToFilter, GenericObservationReportConfig config) {
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        if (config.isEncounterPerRow()) {
            String helperString = "GROUP_CONCAT(DISTINCT(IF(obs_fscn.name = \\'%s\\', coalesce(o.value_numeric, o.value_text, o.value_datetime, coded_scn.name, coded_fscn.name), NULL)) ORDER BY o.obs_id DESC) AS \\'%s\\'";
            for (String conceptName : conceptNamesToFilter) {
                conceptNamesWithDoubleQuote.add(String.format(helperString, conceptName.replace("'", "\\\\\\\'"), conceptName.replace("'", "\\\\\\\'")));
            }
        }
        return StringUtils.join(conceptNamesWithDoubleQuote, ',');
    }

    public static String constructConceptNamesToFilter(List<String> conceptNames) {
        if (CollectionUtils.isEmpty(conceptNames)) {
            return null;
        }
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        for (String conceptName : conceptNames) {
            conceptNamesWithDoubleQuote.add("\"" + conceptName.replace("'", "\\'") + "\"");
        }
        return StringUtils.join(conceptNamesWithDoubleQuote, ',');
    }

    public static String constructConceptValuesToFilter(List<String> conceptValues) {
        if (CollectionUtils.isEmpty(conceptValues)) {
            return null;
        }
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        for (String conceptName : conceptValues) {
            conceptNamesWithDoubleQuote.add("\"" + conceptName.replace("'", "\\'") + "\"");
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

    public static String getConceptNameFormatSql(GenericObservationReportConfig config) {
        String defaultFormat = "CONCAT(coalesce(obs_fscn.name, \"\"), IF(obs_scn.name IS NULL, \"\", CONCAT(\"(\", obs_scn.name, \")\")))";
        if (config == null || config.getConceptNameDisplayFormat() == null)
            return defaultFormat;
        switch (config.getConceptNameDisplayFormat()) {
            case "fullySpecifiedName":
                return "obs_fscn.name";
            case "shortNamePreferred":
                return "IF(obs_scn.name IS NULL,obs_fscn.name,obs_scn.name)";
            default:
                return defaultFormat;
        }
    }

    public static String conceptValuesToFilter(GenericObservationReportConfig config) {
        List<String> listOfConfig = getConceptValuesToFilter(config);
        List<String> parts = new ArrayList<>();
        for (String value : listOfConfig) {
            if (!isNumericRange(value))
                parts.add("\"" + value.replace("'", "\\\\\\\'") + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    private static boolean isNumericRange(String value) {
        return value.contains("..");
    }

    private static List<String> getConceptValuesToFilter(GenericObservationReportConfig config) {
        return config.getConceptValuesToFilter() != null ? config.getConceptValuesToFilter() : new ArrayList<String>();
    }

    public static String constructNumericRangeFilters(GenericObservationReportConfig config) {
        List<String> listOfConfig = getConceptValuesToFilter(config);
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : listOfConfig) {
            if (isNumericRange(value)) {
                if (value.startsWith("..")) {
                    stringBuilder.append(String.format(" OR (o.value_numeric <= %s)", StringUtils.strip(value, "..")));
                } else if (value.endsWith("..")) {
                    stringBuilder.append(String.format(" OR (o.value_numeric >= %s)", StringUtils.strip(value, "..")));
                } else {
                    String[] range = value.split("\\.\\.");
                    stringBuilder.append(String.format(" OR (o.value_numeric BETWEEN %s AND %s)", range[0], range[1]));
                }
            }
        }
        return stringBuilder.toString();
    }

}
