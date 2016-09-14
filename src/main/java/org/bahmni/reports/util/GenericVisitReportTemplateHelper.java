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
import static org.bahmni.reports.model.GenericVisitReportConfig.DateRange.visitStartDate;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericVisitReportTemplateHelper extends GenericReportsHelper {
    public static void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        if (config.isForDataAnalysis()) {
            TextColumnBuilder<Integer> patientIdColumn = col.column("Patient Id", "Patient Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Integer> visitIdColumn = col.column("Visit Id", "Visit Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            jasperReport.addColumn(patientIdColumn);
            jasperReport.addColumn(visitIdColumn);
        }
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
        TextColumnBuilder<String> newPatientVisit = col.column("New patient visit", "New patient visit", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);

        jasperReport.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, patientCreatedDateColumn, visitTypeColumn, dateStartedColumn, dateStoppedColumn, dateOfAdmission, dateOfDischarge,newPatientVisit);
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
        if (config != null && config.getApplyDateRangeFor()!= null) {
            return config.getApplyDateRangeFor().getColumnName();
        }
            return visitStartDate.getColumnName();
    }

}
