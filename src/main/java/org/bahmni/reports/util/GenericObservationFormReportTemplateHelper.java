package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ConceptName;
import org.bahmni.reports.model.GenericObservationFormReportConfig;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.WebClientsException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.fetchConcepts;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getConceptNamesParameter;

public class GenericObservationFormReportTemplateHelper extends GenericReportsHelper {

    private static List<String> getProgramAttributes(GenericObservationFormReportConfig config) {
        return config.getProgramAttributes() != null ? config.getProgramAttributes() : new ArrayList<String>();
    }

    private static List<String> getProgramsToFilter(GenericObservationReportConfig config) {
        return config.getProgramsToFilter() != null ? config.getProgramsToFilter() : new ArrayList<String>();
    }

    public static String constructProgramAttributesString(GenericObservationFormReportConfig config) {
        List<String> programsToFilter = getProgramsToFilter(config);
        List<String> programAttributeTypesToFilter = getProgramAttributes(config);
        List<String> parts = new ArrayList<>();
        if (!programsToFilter.isEmpty()) {
            for (String programAttributeType : programAttributeTypesToFilter) {
                parts.add("\"" + programAttributeType.replace("'", "\\\\\\\'") + "\"");
            }
        }
        return StringUtils.join(parts, ',');
    }

    public static void createAndAddProgramAttributeColumns(JasperReportBuilder jasperReport, GenericObservationFormReportConfig config) {
        for (String programAttribute : getProgramAttributes(config)) {
            TextColumnBuilder<String> column = col.column(programAttribute, programAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static void createAndAddProgramsToFilterColumns(JasperReportBuilder jasperReport, GenericObservationFormReportConfig config) {
        if (!CollectionUtils.isEmpty(config.getProgramsToFilter())) {
            TextColumnBuilder<String> programNameColumn = col.column("Program Name", "Program Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> programEnrollmentDateColumn = col.column("Program Enrollment Date", "Program Enrollment Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> programEndDateColumn = col.column("Program End Date", "Program End Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(programNameColumn, programEnrollmentDateColumn, programEndDateColumn);
            createAndAddProgramAttributeColumns(jasperReport, config);
        }
    }

    public static void createAndAddDefaultColumns(JasperReportBuilder jasperReport, GenericObservationReportConfig config) {
        TextColumnBuilder<String> patientIdentifierColumn = col.column("Patient Identifier", "Patient Identifier", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age", "Age", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> birthdateColumn = col.column("Birthdate", "Birthdate", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> locationNameColumn = col.column("Location Name", "Location Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, locationNameColumn);
    }

    public static void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReport, GenericObservationFormReportConfig config) {
        if (config.isForDataAnalysis()) {
            TextColumnBuilder<Long> patientId = col.column("Patient Id", "Patient Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Long> encounterId = col.column("Encounter Id", "Encounter Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Long> visitId = col.column("Visit Id", "Visit Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            jasperReport.columns(patientId, visitId, encounterId);
        }
    }

    public static List<Integer> fetchConceptIds(Report<GenericObservationFormReportConfig> report, BahmniReportsProperties bahmniReportsProperties) throws WebClientsException {
        List<String> formNamesToFilter = getFormNamesToFilter(report.getConfig());
        if (CollectionUtils.isEmpty(formNamesToFilter)) {
            return new ArrayList<>();
        }
        HttpClient httpClient = report.getHttpClient();
        try {
            String url = bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getConceptId?" + getConceptNamesParameter(formNamesToFilter);
            String response = httpClient.get(new URI(url));
            return new ObjectMapper().readValue(response, new TypeReference<List<Integer>>() {
            });
        } catch (WebClientsException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static List<String> getFormNamesToFilter(GenericObservationFormReportConfig config) {
        return config.getFormNamesToFilter() != null ? config.getFormNamesToFilter() : new ArrayList<String>();
    }


    public static List<ConceptName> fetchLeafConceptsAsList(Report<GenericObservationFormReportConfig> report, BahmniReportsProperties bahmniReportsProperties) throws WebClientsException {
        List<String> formNamesToFilter = getFormNamesToFilter(report.getConfig());
        if (CollectionUtils.isEmpty(formNamesToFilter)) {
            return new ArrayList<>();
        }

        return fetchConcepts(formNamesToFilter, report.getHttpClient(), bahmniReportsProperties);
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
            case "fullySpecifiedName(shortName)":
                if (conceptName.getShortName() == null) {
                    return conceptName.getFullySpecifiedName();
                } else {
                return conceptName.getFullySpecifiedName() + "(" + conceptName.getShortName() + ")";
                }
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
            return conceptName.getShortName();
        }
    }

    public static String constructConceptNameSelectSql(List<String> formNamesToFilter) {
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(obs_fscn.name = \\'%s\\', coalesce(o.value_numeric, o.value_text, o.value_datetime, coded_scn.name, coded_fscn.name), NULL)) ORDER BY o.obs_id DESC) AS \\'%s\\'";
        for (String conceptName : formNamesToFilter) {
            conceptNamesWithDoubleQuote.add(String.format(helperString, conceptName.replace("'", "\\\\\\\'"), conceptName.replace("'", "\\\\\\\'")));
        }
        return StringUtils.join(conceptNamesWithDoubleQuote, ',');
    }

    public static String constructProgramAttributesSql(GenericObservationFormReportConfig config) {
        List<String> programAttributesWithDoubelQuotes = new ArrayList<>();

        String helperString = "GROUP_CONCAT(DISTINCT(IF(prat.name = \\'%s\\', IF( prgrm_fscn.name IS NULL ,coalesce(IF(prat.datatype=\\'org.openmrs.customdatatype.datatype.DateDatatype\\', DATE_FORMAT(ppa.value_reference, \\'%s\\'),ppa.value_reference),NULL), prgrm_fscn.name), NULL))) AS \\'%s\\'";
        String date_format = "%d-%b-%Y";
        List<String> programAttributes = getProgramAttributes(config);
        for (String programAttribute : programAttributes) {
            programAttributesWithDoubelQuotes.add(String.format(helperString, programAttribute.replace("'", "\\\\\\\'"), date_format, programAttribute.replace("'", "\\\\\\\'")));
        }
        return StringUtils.join(programAttributesWithDoubelQuotes, ',');
    }

    public static String constructObsFormConceptIdsToFilter(List<Integer> conceptIds) {
        if (CollectionUtils.isEmpty(conceptIds)) {
            return null;
        }
        StringBuilder formConceptIds = new StringBuilder();
        for (Integer conceptId : conceptIds) {
            formConceptIds.append(conceptId + ",");
        }
        return formConceptIds.substring(0, formConceptIds.length() - 1);
    }


    public static String constructRegexForFormNameAndVersion(List<String> formList){
        if (CollectionUtils.isEmpty(formList)) {
            return null;
        }
        return StringUtils.join(formList, '|');
    }
}
