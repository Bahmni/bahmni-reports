package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.GenericReportsConfig;

import java.util.ArrayList;
import java.util.Arrays;
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
}
