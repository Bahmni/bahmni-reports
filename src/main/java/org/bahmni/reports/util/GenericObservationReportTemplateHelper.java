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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericObservationReportTemplateHelper extends GenericReportsHelper {
    private static HashMap<String,String> conceptNameAndFullySpecifiedName=new HashMap<>();

    public static void createAndAddProviderNameColumn(List<String> allColumns, GenericObservationReportConfig config) {
        if (config.showProvider()) {
            allColumns.add("Provider");
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

    public static void addColumnsToReport(JasperReportBuilder jasperReport, List<String> patientColumns, GenericObservationReportConfig config) {
        if (config != null && config.getColumnsOrder() != null) {
            Collections.reverse(config.getColumnsOrder());
            for (String column : config.getColumnsOrder()) {
                if (patientColumns.contains(column)) {
                    patientColumns.remove(column);
                    patientColumns.add(0, column);
                }
            }
        }
        List<String> conceptNames = new ArrayList<>(conceptNameAndFullySpecifiedName.keySet());
        for (String columnHeader : patientColumns) {
            TextColumnBuilder<String> column;
            if (conceptNames.contains(columnHeader)) {
                column = col.column(columnHeader, conceptNameAndFullySpecifiedName.get(columnHeader), type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            } else {
                column = col.column(columnHeader, columnHeader, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            }
            jasperReport.addColumn(column);
        }

    }

    public static void createAndAddDefaultColumns(List<String> allTheColumns, GenericObservationReportConfig config) {
        List<String> patientColumnHeaders =  new ArrayList<>(Arrays.asList("Patient Identifier","Patient Name","Age","Birthdate", "Gender", "Location Name", "Program Name","Program Enrollment Date","Program End Date","Patient Created Date"));
        allTheColumns.addAll(patientColumnHeaders);
        if (config == null || !config.isEncounterPerRow()) {
            List<String> columnHeadersForEncounterPerRowIsFalse = new ArrayList<>(Arrays.asList("Concept Name", "Value", "Observation Datetime", "Observation Date", "Observation Created Date", "Parent Concept"));
            allTheColumns.addAll(columnHeadersForEncounterPerRowIsFalse);
        }
    }

    public static void createAndAddVisitInfoColumns(List<String> allTheColumns, GenericObservationReportConfig config) {
        if (config.showVisitInfo()) {
            allTheColumns.addAll(Arrays.asList("Visit Type","Visit Start Date", "Visit Stop Date"));
        }
    }

    public static void createAndAddConceptColumns(List<String> allTheColumns, List<ConceptName> conceptNames, String conceptNameDisplayFormat) {
        for (ConceptName conceptName : conceptNames) {
            allTheColumns.add(getConceptInDisplayFormat(conceptName, conceptNameDisplayFormat));
            conceptNameAndFullySpecifiedName.put(getConceptInDisplayFormat(conceptName, conceptNameDisplayFormat),conceptName.getFullySpecifiedName());
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

    public static void createAndAddDataAnalysisColumns( List<String> allTheConceptNames, GenericObservationReportConfig config) {
        if (config.isForDataAnalysis()) {
            if (config.isEncounterPerRow()) {
                allTheConceptNames.addAll(Arrays.asList("Patient Id", "Encounter Id", "Visit Id"));
            } else {
                allTheConceptNames.addAll(Arrays.asList("Patient Id","Visit Id","Encounter Id", "Obs Id","Obs Group Id","Order Id","Concept Id"));
            }
        }
    }

    public static List<ConceptName> fetchLeafConceptsAsList(Report<GenericObservationReportConfig> report, BahmniReportsProperties bahmniReportsProperties) throws WebClientsException {
        List<String> conceptNamesToFilter = getConceptNamesToFilter(report.getConfig());
        if (CollectionUtils.isEmpty(conceptNamesToFilter)) {
            return new ArrayList<>();
        }
        HttpClient httpClient = report.getHttpClient();
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

    public static List<String> getListOfFullySpecifiedNames(List<ConceptName> conceptNames) {
        List<String> fullySpecifiedNames = new ArrayList<>();
        for (ConceptName conceptName : conceptNames)
            fullySpecifiedNames.add(conceptName.getFullySpecifiedName());
        return fullySpecifiedNames;
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
