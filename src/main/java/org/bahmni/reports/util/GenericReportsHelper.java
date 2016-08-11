package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.GenericReportsConfig;

import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericReportsHelper {

    public static void createAndAddExtraPatientIdentifierTypes(JasperReportBuilder jasperReport, GenericReportsConfig config) {
        for (String patientIdentifierType : config.getAdditionalPatientIdentifiers()) {
            TextColumnBuilder<String> patientIdentifierColumn = col.column(patientIdentifierType, patientIdentifierType, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(patientIdentifierColumn);
        }
    }

    public static String constructExtraPatientIdentifiersToFilter(GenericReportsConfig config) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pit.name = \\'%s\\', pi.identifier, NULL))) AS \\'%s\\'";

        for (String patientIdentifierType : config.getAdditionalPatientIdentifiers()) {
            parts.add(String.format(helperString, patientIdentifierType.replace("'", "\\\\\\\'"), patientIdentifierType.replace("'", "\\\\\\\'")));
        }
        return StringUtils.join(parts, ", ");
    }

    public static String constructPatientAttributeNamesToDisplay(GenericReportsConfig config) {
        List<String> patientAttributes = config.getPatientAttributes();
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pat.name = \\'%s\\', IF(pat.format = \\'org.openmrs.Concept\\',coalesce(scn.name, fscn.name),pa.value), NULL))) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute.replace("'", "\\\\\\\'"), patientAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");
    }

    public static String constructVisitAttributeNamesToDisplay(GenericReportsConfig config) {
        List<String> visitAttributes = config.getVisitAttributes();
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(vat.name = \\'%s\\', va.value_reference, NULL))) AS \\'%s\\'";

        for (String visitAttribute : visitAttributes) {
            parts.add(String.format(helperString, visitAttribute.replace("'", "\\\\\\\'"), visitAttribute.replace("'", "\\\\\\\'")));
        }

        return StringUtils.join(parts, ", ");

    }

    public static String constructPatientAddressesToDisplay(GenericReportsConfig config) {
        List<String> patientAddresses = config.getPatientAddresses();
        StringBuilder stringBuilder = new StringBuilder();
        if (patientAddresses != null) {
            for (String address : patientAddresses) {
                stringBuilder.append("paddress").append(".").append(address).append(", ");
            }
        }
        return stringBuilder.toString();
    }
}
