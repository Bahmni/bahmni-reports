package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.GenericReportsConfig;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericReportsHelper {

    public static void createAndAddExtraPatientIdentifierTypes(JasperReportBuilder jasperReport, GenericReportsConfig config) {
        for (String patientIdentifierType : getExtraPatientIdentifierTypes(config)) {
            TextColumnBuilder<String> patientIdentifierColumn = col.column(patientIdentifierType, patientIdentifierType, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(patientIdentifierColumn);
        }
    }

    private static List<String> getExtraPatientIdentifierTypes(GenericReportsConfig reportsConfig) {
        return reportsConfig.getAdditionalPatientIdentifiers() != null ? reportsConfig.getAdditionalPatientIdentifiers() : Arrays.<String>asList();
    }

    public static String constructExtraPatientIdentifiersToFilter(GenericReportsConfig config) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pit.name = \\'%s\\', pi.identifier, NULL))) AS \\'%s\\'";

        for (String patientIdentifierType : getExtraPatientIdentifierTypes(config)) {
            parts.add(String.format(helperString, patientIdentifierType.replace("'", "\\\\\\\'"), patientIdentifierType.replace("'", "\\\\\\\'")));
        }
        return StringUtils.join(parts, ", ");
    }

    public static void createAndAddPatientAttributeColumns(JasperReportBuilder jasperReport, GenericReportsConfig config) {
        for (String patientAttribute : getPatientAttributes(config)) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static void createAndAddVisitAttributeColumns(JasperReportBuilder jasperReport, GenericReportsConfig config) {
        for (String visitAttribute : getVisitAttributes(config)) {
            TextColumnBuilder<String> column = col.column(visitAttribute, visitAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private static List<String> getPatientAttributes(GenericReportsConfig config) {
        return config.getPatientAttributes() != null ? config.getPatientAttributes() : new ArrayList<String>();
    }

    private static List<String> getVisitAttributes(GenericReportsConfig config) {
        return config.getVisitAttributes() != null ? config.getVisitAttributes() : new ArrayList<String>();
    }

    public static void createAndAddPatientAddressColumns(JasperReportBuilder jasperReport, GenericReportsConfig config) {
        for (String address : getPatientAddresses(config)) {
            TextColumnBuilder<String> column = col.column(address, address, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static String constructPatientAttributeNamesToDisplay(GenericReportsConfig config) {
        List<String> patientAttributes = getPatientAttributes(config);
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pat.name = \\'%s\\', IF(pat.format = \\'org.openmrs.Concept\\',coalesce(scn.name, fscn.name),pa.value), NULL))) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute.replace("'", "\\\\\\\'"), patientAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");
    }

    private static List<String> getPatientAddresses(GenericReportsConfig config) {
        return config.getPatientAddresses() != null ? config.getPatientAddresses() : new ArrayList<String>();
    }

    public static String constructPatientAddressesToDisplay(GenericReportsConfig config) {
        List<String> patientAddresses = getPatientAddresses(config);
        StringBuilder stringBuilder = new StringBuilder();
        if (patientAddresses != null) {
            for (String address : patientAddresses) {
                stringBuilder.append("paddress").append(".").append(address).append(", ");
            }
        }
        return stringBuilder.toString();
    }

    public static String constructVisitAttributeNamesToDisplay(GenericReportsConfig config) {
        List<String> visitAttributes = getVisitAttributes(config);
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(vat.name = \\'%s\\', va.value_reference, NULL))) AS \\'%s\\'";

        for (String visitAttribute : visitAttributes) {
            parts.add(String.format(helperString, visitAttribute.replace("'", "\\\\\\\'"), visitAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");

    }

    public static void createAndAddAgeGroupColumn(JasperReportBuilder jasperReport, GenericReportsConfig config) {
        if (StringUtils.isEmpty(config.getAgeGroupName())) return;
        TextColumnBuilder<String> ageGroupColumn = col.column(config.getAgeGroupName(), config.getAgeGroupName(), type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.addColumn(ageGroupColumn);
    }

    public static String constructSortByColumnsOrder(GenericReportsConfig config) throws InvalidConfigurationException {
        List<HashMap<String, String>> sortOrder = config.getSortBy();
        String helperString = "ORDER BY ";
        for (HashMap<String, String> columns : sortOrder) {
            if (columns.get("column") == null) {
                throw new InvalidConfigurationException("Column is not configured in sortBy");
            }
            if (columns.get("sortOrder") == null) {
                columns.put("sortOrder", "asc");
            }
            if (!(columns.get("sortOrder").equalsIgnoreCase("asc") || columns.get("sortOrder").equalsIgnoreCase("desc"))) {
                throw new InvalidConfigurationException("Invalid sortOrder in sortBy config. Only asc or desc with case insensitivity is allowed");
            }
            helperString = helperString.concat('`' + columns.get("column") + "` " + columns.get("sortOrder").toUpperCase() + ", ");
        }
        helperString = helperString.substring(0, helperString.lastIndexOf(","));
        return helperString;
    }
}
