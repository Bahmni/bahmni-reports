package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.GenericLabOrderReportConfig;
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

public class GenericLabOrderReportTemplateHelper {

    public static void createAndAddPatientAttributeColumns(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        for (String patientAttribute : getPatientAttributes(config)) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReportBuilder.addColumn(column);
        }
    }

    public static void createAndAddVisitAttributeColumns(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        for (String visitAttribute : getVisitAttributes(config)) {
            TextColumnBuilder<String> column = col.column(visitAttribute, visitAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReportBuilder.addColumn(column);
        }
    }

    public static void createAndAddProviderNameColumn(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        if (config.showProvider()) {
            TextColumnBuilder<String> providerColumn = col.column("Provider", "Provider", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReportBuilder.addColumn(providerColumn);
        }
    }

    public static void createAndAddPatientAddressColumns(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        for (String address : getPatientAddresses(config)) {
            TextColumnBuilder<String> column = col.column(address, address, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReportBuilder.addColumn(column);
        }
    }

    public static String constructPatientAttributeNamesToDisplay(GenericLabOrderReportConfig config) {
        List<String> patientAttributes = getPatientAttributes(config);
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pat.name = \\'%s\\', IF(pat.format = \\'org.openmrs.Concept\\',coalesce(scn.name, fscn.name),pa.value), NULL))) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute.replace("'", "\\\\\\\'"), patientAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");
    }

    private static List<String> getPatientAttributes(GenericLabOrderReportConfig config) {
        return config.getPatientAttributes() != null ? config.getPatientAttributes() : new ArrayList<String>();
    }

    private static List<String> getVisitAttributes(GenericLabOrderReportConfig config) {
        return config.getVisitAttributes() != null ? config.getVisitAttributes() : new ArrayList<String>();
    }

    private static List<String> getPatientAddresses(GenericLabOrderReportConfig config) {
        return config.getPatientAddresses() != null ? config.getPatientAddresses() : new ArrayList<String>();
    }

    private static List<String> getProgramsToFilter(GenericLabOrderReportConfig config) {
        return config.getProgramsToFilter() != null ? config.getProgramsToFilter() : new ArrayList<String>();
    }

    public static String constructPatientAddressesToDisplay(GenericLabOrderReportConfig config) {
        List<String> patientAddresses = getPatientAddresses(config);
        StringBuilder stringBuilder = new StringBuilder();
        if (patientAddresses != null) {
            for (String address : patientAddresses) {
                stringBuilder.append("paddress").append(".").append(address).append(", ");
            }
        }
        return stringBuilder.toString();
    }

    public static String constructVisitAttributeNamesToDisplay(GenericLabOrderReportConfig config) {
        List<String> visitAttributes = getVisitAttributes(config);
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(vat.name = \\'%s\\', va.value_reference, NULL))) AS \\'%s\\'";

        for (String visitAttribute : visitAttributes) {
            parts.add(String.format(helperString, visitAttribute.replace("'", "\\\\\\\'"), visitAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");
    }

    public static String constructProgramsString(GenericLabOrderReportConfig config) {
        List<String> programsToFilter = getProgramsToFilter(config);
        List<String> parts = new ArrayList<>();
        for (String programName : programsToFilter) {
            parts.add("\"" + programName.replace("'", "\\\\\\\'") + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    public static void createAndAddMandatoryColumns(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        TextColumnBuilder<String> patientIdentifierColumn = col.column("Patient Identifier", "Patient Identifier", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age", "Age", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> birthdateColumn = col.column("Birthdate", "Birthdate", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> testOrderDate = col.column("Test Order Date", "Test Order Date", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> testName = col.column("Test Name", "Test Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> testResult = col.column("Test Result", "Test Result", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> testOutcome = col.column("Test Outcome", "Test Outcome", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> minRange = col.column("Min Range", "Min Range", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> maxRange = col.column("Max Range", "Max Range", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReportBuilder.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, testOrderDate, testName, testResult, testOutcome, minRange, maxRange);
    }

    public static void createAndAddVisitInfoColumns(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        if (config.showVisitInfo()) {
            TextColumnBuilder<String> visitTypeColumn = col.column("Visit Type", "Visit Type", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<Date> visitStartDateColumn = col.column("Visit Start Date", "Visit Start Date", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<Date> visitStopDateColumn = col.column("Visit Stop Date", "Visit Stop Date", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReportBuilder.columns(visitTypeColumn, visitStartDateColumn, visitStopDateColumn);
        }
    }

    public static void createAndAddProgramNameColumn(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        if (getProgramsToFilter(config).size() > 0) {
            TextColumnBuilder<String> programNameColumn = col.column("Program Name", "Program Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReportBuilder.addColumn(programNameColumn);
        }
    }

    public static void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        if (config.isForDataAnalysis()) {
            TextColumnBuilder<Long> patientId = col.column("Patient Id", "Patient Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Long> conceptId = col.column("Concept Id", "Concept Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Long> obsId = col.column("Obs Id", "Obs Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Long> orderId = col.column("Order Id", "Order Id", type.longType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            jasperReportBuilder.columns(patientId, conceptId, obsId, orderId);

        }
    }

    public static void showOrderDateTime(JasperReportBuilder jasperReportBuilder, GenericLabOrderReportConfig config) {
        if (config != null && config.isShowOrderDateTime()) {
            TextColumnBuilder<String> orderDateTime = col.column("Order DateTime", "order_date_created", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReportBuilder.addColumn(orderDateTime);
        }
    }

    private static List<String> fetchChildConceptsAsList(List<String> conceptNamesToFilter, Report<GenericLabOrderReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
        if (CollectionUtils.isEmpty(conceptNamesToFilter)) {
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

    public static String constructConceptNamesToFilter(Report<GenericLabOrderReportConfig> report, BahmniReportsProperties bahmniReportsProperties) {
        List<String> conceptNamesToFilter = getConceptNamesToFilter(report.getConfig());
        List<String> childConceptsAsList = fetchChildConceptsAsList(conceptNamesToFilter, report, bahmniReportsProperties);
        if (CollectionUtils.isEmpty(childConceptsAsList)) {
            return null;
        }
        List<String> conceptNamesWithDoubleQuote = new ArrayList<>();
        for (String conceptName : childConceptsAsList) {
            conceptNamesWithDoubleQuote.add("\"" + conceptName.replace("'", "\\'") + "\"");
        }
        return StringUtils.join(conceptNamesWithDoubleQuote, ',');
    }

    private static List<String> getConceptNamesToFilter(GenericLabOrderReportConfig config) {
        return config.getConceptNamesToFilter() != null ? config.getConceptNamesToFilter() : new ArrayList<String>();
    }

    public static String conceptValuesToFilter(GenericLabOrderReportConfig config) {
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

    private static List<String> getConceptValuesToFilter(GenericLabOrderReportConfig config) {
        return config.getConceptValuesToFilter() != null ? config.getConceptValuesToFilter() : new ArrayList<String>();
    }

    public static String constructNumericRangeFilters(GenericLabOrderReportConfig config) {
        List<String> listOfConfig = getConceptValuesToFilter(config);
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : listOfConfig) {
            if (isNumericRange(value)) {
                if (value.startsWith("..")) {
                    stringBuilder.append(String.format(" OR (bigTable.value_numeric <= %s)", StringUtils.strip(value, "..")));
                } else if (value.endsWith("..")) {
                    stringBuilder.append(String.format(" OR (bigTable.value_numeric >= %s)", StringUtils.strip(value, "..")));
                } else {
                    String[] range = value.split("\\.\\.");
                    stringBuilder.append(String.format(" OR (bigTable.value_numeric BETWEEN %s AND %s)", range[0], range[1]));
                }
            }
        }
        return stringBuilder.toString();
    }
}
