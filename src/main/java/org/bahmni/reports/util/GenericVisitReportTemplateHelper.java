package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.GenericVisitReportConfig;

import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericVisitReportTemplateHelper {
    public static void createAndAddVisitAttributeColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        for (String visitAttribute : getVisitAttributes(config)) {
            TextColumnBuilder<String> column = col.column(visitAttribute, visitAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static void createAndAddPatientAddressColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        for (String address : getPatientAddresses(config)) {
            TextColumnBuilder<String> column = col.column(address, address, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static void createAndAddPatientAttributeColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        for (String patientAttribute : getPatientAttributes(config)) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public static void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        if (config.isForDataAnalysis()) {
            TextColumnBuilder<Integer> patientIdColumn = col.column("Patient Id", "Patient Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Integer> visitIdColumn = col.column("Visit Id", "Visit Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            jasperReport.addColumn(patientIdColumn);
            jasperReport.addColumn(visitIdColumn);
        }
    }
    public static void createAndAddAgeGroupColumn(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        if (StringUtils.isEmpty(config.getAgeGroupName())) return;
        TextColumnBuilder<String> ageGroupColumn = col.column(config.getAgeGroupName(), config.getAgeGroupName(), type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.addColumn(ageGroupColumn);
    }

    public static void createAndAddMandatoryColumns(JasperReportBuilder jasperReport) {
        TextColumnBuilder<String> patientIdentifierColumn = col.column("Patient Identifier", "Patient Identifier", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age", "Age", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> birthdateColumn = col.column("Birthdate", "Birthdate", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientCreatedDateColumn = col.column("Patient Created Date", "Patient Created Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<String> visitTypeColumn = col.column("Visit Type", "Visit Type", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> dateStartedColumn = col.column("Date started", "Date started", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> dateStoppedColumn = col.column("Date stopped", "Date stopped", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> dateOfAdmission = col.column("Date Of Admission", "Date Of Admission", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> dateOfDischarge = col.column("Date Of Discharge", "Date Of Discharge", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);

        jasperReport.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, patientCreatedDateColumn, visitTypeColumn, dateStartedColumn, dateStoppedColumn, dateOfAdmission, dateOfDischarge);
    }

    public static List<String> getPatientAttributes(GenericVisitReportConfig config) {
        return config.getPatientAttributes() != null ? config.getPatientAttributes() : new ArrayList<String>();
    }

    public static List<String> getVisitAttributes(GenericVisitReportConfig config) {
        return config.getVisitAttributes() != null ? config.getVisitAttributes() : new ArrayList<String>();
    }

    public static List<String> getPatientAddresses(GenericVisitReportConfig config) {
        return config.getPatientAddresses() != null ? config.getPatientAddresses() : new ArrayList<String>();
    }

    public static String constructPatientAttributeNamesString(List<String> patientAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pat.name = \\'%s\\', IF(pat.format = \\'org.openmrs.Concept\\',coalesce(scn.name, fscn.name),pa.value), NULL))) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute, patientAttribute));
        }

        return StringUtils.join(parts, ", ");
    }

    public static String constructPatientAddresses(List<String> patientAddresses) {
        StringBuilder stringBuilder = new StringBuilder();
        if (patientAddresses != null) {
            for (String address : patientAddresses) {
                stringBuilder.append("paddress").append(".").append(address).append(", ");
            }
        }
        return stringBuilder.toString();
    }

    public static String constructVisitAttributeNamesString(List<String> visitAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(vat.name = \\'%s\\', va.value_reference, NULL))) AS \\'%s\\'";

        for (String visitAttribute : visitAttributes) {
            parts.add(String.format(helperString, visitAttribute, visitAttribute));
        }

        return StringUtils.join(parts, ", ");
    }

    public static String constructVisitTypesString(List<String> visitTypesToFilter) {
        List<String> parts = new ArrayList<>();
        for (String visitType : visitTypesToFilter) {
            parts.add("\"" + visitType + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    public static List<String> getVisitTypesToFilter(GenericVisitReportConfig config) {
        return config.getVisitTypesToFilter() != null ? config.getVisitTypesToFilter() : new ArrayList<String>();
    }

    public static String getDateRangeFor(GenericVisitReportConfig config) {
        if (config != null && "visitStopDate".equals(config.getApplyDateRangeFor())) {
            return "v.date_stopped";
        }
        return "v.date_started";
    }

}
