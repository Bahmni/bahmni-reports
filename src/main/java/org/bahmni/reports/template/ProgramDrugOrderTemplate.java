package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.ProgramDrugOrderTemplateConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.columnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class ProgramDrugOrderTemplate extends BaseReportTemplate<ProgramDrugOrderTemplateConfig> {


    private String getFormattedSql(String fileContent, List<String> patientAttributes, List<String> programAttributes, List<String> programs, String startDate, String endDate) {
        ST sqlTemplate = new ST(fileContent, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("patientAttributesInClauseEscapeQuote", getAttributesInClause(patientAttributes));
        sqlTemplate.add("programAttributesInClauseEscapeQuote", getAttributesInClause(programAttributes));
        sqlTemplate.add("programNamesInClauseEscapeQuote", getAttributesInClause(programs));
        sqlTemplate.add("patientAttributes", constructPatientAttributeNamesString(patientAttributes));
        sqlTemplate.add("programAttributes", constructProgramAttributeNamesString(programAttributes));

        return sqlTemplate.render();
    }



    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ProgramDrugOrderTemplateConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);
        ProgramDrugOrderTemplateConfig programDrugOrderTemplateConfig = report.getConfig();

        List<String> patientAttributes = programDrugOrderTemplateConfig.getPatientAttributes();
        if (patientAttributes == null) {
            patientAttributes = new ArrayList<>();
        }
        List<String> programAttributes = programDrugOrderTemplateConfig.getProgramAttributes();
        if (programAttributes == null) {
            programAttributes = new ArrayList<>();
        }

        List<String> programs = programDrugOrderTemplateConfig.getPrograms();
        if (programs == null) {
            programs = new ArrayList<>();
        }

            String sql = getFormattedSql(getFileContent("sql/programDrugOrder.sql"),patientAttributes ,programAttributes,
               programs, startDate, endDate );
        buildColumns(jasperReport, patientAttributes, programAttributes);

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);
        JasperReportBuilder jasperReportBuilder = SqlUtil.executeReportWithStoredProc(jasperReport, connection, sql);
        return new BahmniReportBuilder(jasperReportBuilder);
    }

    private void buildColumns(JasperReportBuilder jasperReport, List<String> patientAttributes, List<String> programAttributes) {
        TextColumnBuilder<String> drugName = col.column("Drug Name", "drugName", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> dose = col.column("Dose", "dose", type.stringType())
                .setValueFormatter(new DoseValueFormatter())
                .setStyle(columnStyle);

        TextColumnBuilder<String> doseUnits = col.column("Unit", "unit", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> frequency = col.column("Frequency", "frequency", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> duration = col.column("Duration", "duration", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> route = col.column("Route", "route", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<Date> startdate = col.column("Start Date", "startDate", type.dateType())
                .setStyle(columnStyle);

        TextColumnBuilder<Date> stopDate = col.column("Stop Date", "stopDate", type.dateType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> quantity = col.column("Quantity", "quantity", type.stringType()).setStyle(columnStyle);

        TextColumnBuilder<String> programName = col.column("Program Name", "programName", type.stringType()).setStyle(columnStyle);


        TextColumnBuilder<String> patientName = col.column("Patient Name", "patientName", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> patientId = col.column("Patient ID", "patientId", type.stringType())
                .setStyle(columnStyle);

        TextColumnBuilder<Integer> age = col.column("Age", "age", type.integerType())
                .setStyle(columnStyle);

        TextColumnBuilder<String> gender = col.column("Gender", "gender", type.stringType())
                .setStyle(columnStyle);


        jasperReport.columns(patientId, patientName, age, gender, programName, drugName, dose,
                 doseUnits,  frequency, duration, route, quantity, startdate,
                stopDate);

        for (String patientAttribute : patientAttributes) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType());
            jasperReport.addColumn(column);
        }
        for (String programAttribute : programAttributes) {
            TextColumnBuilder<String> column = col.column(programAttribute, programAttribute, type.stringType());
            jasperReport.addColumn(column);
        }


    }

    private class DoseValueFormatter implements DRIValueFormatter<String, String> {
        private static final long serialVersionUID = 1L;

        @Override
        public String format(String data, ReportParameters reportParameters) {
            String medication_as_needed = "(PRN)";
            String med = "", dose = data;
            if (data.endsWith(medication_as_needed)){
                dose = data.substring(0, data.length() - medication_as_needed.length());
                med = medication_as_needed;
            }

            if (dose.matches("(^\\{)(.*)(\\}$)")) {
                ObjectMapper mapper = new ObjectMapper();

                try {
                    VariableDosageFormatter dosage = mapper.readValue(dose, VariableDosageFormatter.class);
                    return dosage.getMorningDose() + "-" + dosage.getAfternoonDose() + "-" + dosage.getEveningDose() + med;

                } catch (IOException e) {
                    return e.toString();
                }
            }

            return data ;
        }

        @Override
        public Class<String> getValueClass() {
            return String.class;
        }
    }

    private static class VariableDosageFormatter {
        private String morningDose;
        private String afternoonDose;
        private String eveningDose;
        private String instructions;
        private String additionalInstructions;



        public String getMorningDose() {
            return morningDose;
        }

        public String getAfternoonDose() {
            return afternoonDose;
        }

        public String getEveningDose() {
            return eveningDose;
        }

        public String getInstructions() {
            return instructions;
        }

        public String getAdditionalInstructions() {
            return additionalInstructions;
        }


    }

    private String constructPatientAttributeNamesString(List<String> patientAttributes) {
        ArrayList<String> parts = new ArrayList<>();
        String helperString = "MAX(IF(o.patient_attribute_name = \\'%s\\', o.patient_attribute_value, NULL)) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute, patientAttribute));
        }

        return StringUtils.join(parts, ", ");
    }

    private Object constructProgramAttributeNamesString(List<String> programAttributes) {
        ArrayList<String> parts = new ArrayList<>();
        String helperString = "MAX(IF(o.program_attribute_name = \\'%s\\', o.program_attribute_value, NULL)) AS \\'%s\\'";

        for (String programAttribute : programAttributes) {
            parts.add(String.format(helperString, programAttribute, programAttribute));
        }

        return StringUtils.join(parts, ", ");
    }



    private String getAttributesInClause(List<String> parameters) {
        List<String> convertedList = new ArrayList<>();
        if (parameters.isEmpty()) {
            return "''";
        }
        for (String parameter : parameters) {
            convertedList.add(encloseWithQuotes(parameter));
        }
        return escapeQuotes(StringUtils.join(convertedList, ","));
    }

    private String escapeQuotes(String inclause) {
        return inclause.replace("'", "\\'");
    }
    private String encloseWithQuotes(String input) {
        return "'" + input + "'";
    }

}
