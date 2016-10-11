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
import java.util.Arrays;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericLabOrderReportTemplateHelper extends GenericReportsHelper{

    public static void createAndAddProviderNameColumn(List<String> allColumnsForReport, GenericLabOrderReportConfig config) {
        if (config.showProvider()) {
            allColumnsForReport.add("Provider");
        }
    }

    private static List<String> getProgramsToFilter(GenericLabOrderReportConfig config) {
        return config.getProgramsToFilter() != null ? config.getProgramsToFilter() : new ArrayList<String>();
    }

    public static String constructProgramsString(GenericLabOrderReportConfig config) {
        List<String> programsToFilter = getProgramsToFilter(config);
        List<String> parts = new ArrayList<>();
        for (String programName : programsToFilter) {
            parts.add("\"" + programName.replace("'", "\\\\\\\'") + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    public static void createAndAddDefaultColumns(List<String> allColumnsForReport, GenericLabOrderReportConfig config) {
        allColumnsForReport.addAll(Arrays.asList("Patient Identifier", "Patient Name", "Age","Birthdate","Gender","Test Order Date","Test Name", "Test Result","Test Outcome","Min Range","Max Range","File Uploaded"));
        createAndAddReferredOutColumn(allColumnsForReport,config);
    }

    public static void createAndAddVisitInfoColumns(List<String> allColumnNamesForFilter, GenericLabOrderReportConfig config) {
        if (config.showVisitInfo()) {
            allColumnNamesForFilter.addAll(Arrays.asList("Visit Type","Visit Start Date","Visit Stop Date"));
        }
    }

    public static void createAndAddProgramNameColumn(List<String> allColumnsForReport, GenericLabOrderReportConfig config) {
        if (getProgramsToFilter(config).size() > 0) {
            allColumnsForReport.add("Program Name");
        }
    }

    public static void createAndAddDataAnalysisColumns(List<String> allColumnsForTheReport, GenericLabOrderReportConfig config) {
        if (config.isForDataAnalysis()) {
            allColumnsForTheReport.addAll(Arrays.asList("Patient Id","Concept Id","Obs Id","Order Id"));
        }
    }

    public static void showOrderDateTime(List<String> allColumnsForTheReport, GenericLabOrderReportConfig config) {
        if (config != null && config.isShowOrderDateTime()) {
            allColumnsForTheReport.add("Order DateTime");
        }
    }

    private static void createAndAddReferredOutColumn(List<String> patientColumns, GenericLabOrderReportConfig config) {
        if (config == null || config.showReferredOutTests()) {
            patientColumns.add("Referred Out");
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

    public static void addColumnsToReport(JasperReportBuilder jasperReport, List<String> patientColumns) {
        for (String columnHeader : patientColumns) {
            TextColumnBuilder<String> column;
            column = col.column(columnHeader, columnHeader, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }

    }
}
