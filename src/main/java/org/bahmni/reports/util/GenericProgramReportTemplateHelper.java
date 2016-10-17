package org.bahmni.reports.util;


import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.GenericProgramReportConfig;

import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;

public class GenericProgramReportTemplateHelper extends GenericReportsHelper{

    public static  String constructProgramAttributeNamesString(List<String> programAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(prat.name = \\'%s\\', IF(prat.datatype = \\'org.bahmni.module.bahmnicore.customdatatype.datatype.CodedConceptDatatype\\',coalesce(pratsn.name, pratfn.name),ppa.value_reference), NULL))) AS \\'%s\\'";
        for (String patientAttribute : programAttributes) {
            parts.add(String.format(helperString, patientAttribute, patientAttribute));
        }

        return StringUtils.join(parts, ", ");
    }

    public  static String constructProgramNamesString(List<String> programNamesToFilter) {
        List<String> parts = new ArrayList<>();
        for (String programName : programNamesToFilter) {
            parts.add("\"" + programName + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    public static List<String> getProgramAttributes(GenericProgramReportConfig config) {
        return config.getProgramAttributes() != null ? config.getProgramAttributes() : new ArrayList<String>();
    }

    public static List<String> getProgramNamesToFilter(GenericProgramReportConfig config) {
        return config.getProgramNamesToFilter() != null ? config.getProgramNamesToFilter() : new ArrayList<String>();
    }


    public static String getDateRangeFor(GenericProgramReportConfig config) {
        if (config != null && "visitStopDate".equals(config.getApplyDateRangeFor())) {
            return "v.date_stopped";
        }
        return "v.date_started";
    }

    public static void createAndAddProgramAttributeColumns(JasperReportBuilder jasperReport, GenericProgramReportConfig config) {
        for (String programAttribute : getProgramAttributes(config)) {
            TextColumnBuilder<String> column = col.column(programAttribute, programAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    public  static void createAndAddMandatoryColumns(JasperReportBuilder jasperReport) {
        TextColumnBuilder<String> patientIdentifierColumn = col.column("Patient Identifier", "Patient Identifier", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age", "Age", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> birthdateColumn = col.column("Birthdate", "Birthdate", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientCreatedDateColumn = col.column("Patient Created Date", "Patient Created Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> programNameColumn = col.column("Program Name", "Program Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> dateEnrolledColumn = col.column("Enrolled Date", "Enrolled Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> dateCompletedColumn = col.column("Completed Date", "Completed Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, patientCreatedDateColumn, programNameColumn, dateEnrolledColumn, dateCompletedColumn);
    }



    public static void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReport, GenericProgramReportConfig config) {
        if (config.isForDataAnalysis()) {
            TextColumnBuilder<Integer> patientIdColumn = col.column("Patient Id", "Patient Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<Integer> visitIdColumn = col.column("Program Id", "Program Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> outcomeColumn = col.column("OutCome", "OutCome", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(patientIdColumn);
            jasperReport.addColumn(visitIdColumn);
            jasperReport.addColumn(outcomeColumn);
        }
    }

    public static void createAndAddProgramStatesColumns(JasperReportBuilder jasperReport, GenericProgramReportConfig config) {
        if(config != null && config.isShowAllStates()){
            TextColumnBuilder<String> stateColumn = col.column("State", "Current State", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> startDateColumn = col.column("Start Date", "Start Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> endDateColumn = col.column("End Date", "End Date", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(stateColumn);
            jasperReport.addColumn(startDateColumn);
            jasperReport.addColumn(endDateColumn);

        }else{
            TextColumnBuilder<String> currentStateColumn = col.column("Current State", "Current State", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(currentStateColumn);
        }
    }
}
