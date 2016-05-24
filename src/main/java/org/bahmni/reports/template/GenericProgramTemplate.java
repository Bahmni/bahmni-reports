package org.bahmni.reports.template;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.GenericProgramReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;

import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import org.bahmni.reports.util.CommonComponents;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

@UsingDatasource("openmrs")
public class GenericProgramTemplate extends BaseReportTemplate<GenericProgramReportConfig> {
    private BahmniReportsProperties bahmniReportsProperties;

    public GenericProgramTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<GenericProgramReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);

        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        createAndAddMandatoryColumns(jasperReport);
        createAndAddProgramStatesColumns(jasperReport, report.getConfig());
        if (report.getConfig() != null) {
            createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
            createAndAddProgramAttributeColumns(jasperReport, report.getConfig());
            createAndAddPatientAddressColumns(jasperReport, report.getConfig());
            createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
        }

        String formattedSql = getFormattedSql(report.getConfig(), startDate, endDate);
        return SqlUtil.executeReportWithStoredProc(jasperReport, connection, formattedSql);
    }


    private String getFormattedSql(GenericProgramReportConfig config, String startDate, String endDate) {
        String sql = getFileContent("sql/genericProgramReport.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (config != null) {
            sqlTemplate.add("patientAttributes", constructPatientAttributeNamesString(getPatientAttributes(config)));
            sqlTemplate.add("patientAddresses", constructPatientAddresses(getPatientAddresses(config)));
            sqlTemplate.add("programAttributes", constructProgramAttributeNamesString(getProgramAttributes(config)));
            sqlTemplate.add("showAllStates", config.isShowAllStates());
            sqlTemplate.add("programNamesToFilterSql", constructProgramNamesString(getProgramNamesToFilter(config)));
        }
        return sqlTemplate.render();
    }


    private void createAndAddMandatoryColumns(JasperReportBuilder jasperReport) {
        TextColumnBuilder<String> patientIdentifierColumn = col.column("Patient Identifier", "Patient Identifier", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age", "Age", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> birthdateColumn = col.column("Birthdate", "Birthdate", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> programNameColumn = col.column("Program Name", "Program Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<Date> dateEnrolledColumn = col.column("Enrolled Date", "Enrolled Date", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> dateCompletedColumn = col.column("Completed Date", "Completed Date", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, programNameColumn, dateEnrolledColumn, dateCompletedColumn);
    }

    private void createAndAddPatientAttributeColumns(JasperReportBuilder jasperReport, GenericProgramReportConfig config) {
        for (String patientAttribute : getPatientAttributes(config)) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }


    private void createAndAddPatientAddressColumns(JasperReportBuilder jasperReport, GenericProgramReportConfig config) {
        for (String address : getPatientAddresses(config)) {
            TextColumnBuilder<String> column = col.column(address, address, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }


    private void createAndAddProgramAttributeColumns(JasperReportBuilder jasperReport, GenericProgramReportConfig config) {
        for (String programAttribute : getProgramAttributes(config)) {
            TextColumnBuilder<String> column = col.column(programAttribute, programAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReport, GenericProgramReportConfig config) {
        if (config.isForDataAnalysis()) {
            TextColumnBuilder<Integer> patientIdColumn = col.column("Patient Id", "Patient Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<Integer> visitIdColumn = col.column("Program Id", "Program Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<String> outcomeColumn = col.column("OutCome", "Outcome", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(patientIdColumn);
            jasperReport.addColumn(visitIdColumn);
            jasperReport.addColumn(outcomeColumn);
        }
    }

    private void createAndAddProgramStatesColumns(JasperReportBuilder jasperReport, GenericProgramReportConfig config) {
        if(config != null && config.isShowAllStates()){
            TextColumnBuilder<String> stateColumn = col.column("State", "Current State", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<Date> startDateColumn = col.column("Start Date", "Start Date", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            TextColumnBuilder<Date> endDateColumn = col.column("End Date", "End Date", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(stateColumn);
            jasperReport.addColumn(startDateColumn);
            jasperReport.addColumn(endDateColumn);

        }else{
            TextColumnBuilder<String> currentStateColumn = col.column("Current State", "Current State", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(currentStateColumn);
        }
    }

    private String constructPatientAttributeNamesString(List<String> patientAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pat.name = \\'%s\\', IF(pat.format = \\'org.openmrs.Concept\\',coalesce(scn.name, fscn.name),pa.value), NULL))) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute, patientAttribute));
        }

        return StringUtils.join(parts, ", ");
    }

    private String constructProgramAttributeNamesString(List<String> programAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(prat.name = \\'%s\\', IF(prat.datatype = \\'org.bahmni.module.bahmnicore.customdatatype.datatype.CodedConceptDatatype\\',coalesce(pratsn.name, pratfn.name),ppa.value_reference), NULL))) AS \\'%s\\'";
        for (String patientAttribute : programAttributes) {
            parts.add(String.format(helperString, patientAttribute, patientAttribute));
        }

        return StringUtils.join(parts, ", ");
    }

    private String constructPatientAddresses(List<String> patientAddresses) {
        StringBuilder stringBuilder = new StringBuilder();
        if (patientAddresses != null) {
            for (String address : patientAddresses) {
                stringBuilder.append("paddress").append(".").append(address).append(", ");
            }
        }
        return stringBuilder.toString();
    }

    private String constructProgramNamesString(List<String> programNamesToFilter) {
        List<String> parts = new ArrayList<>();
        for (String programName : programNamesToFilter) {
            parts.add("\"" + programName + "\"");
        }
        return StringUtils.join(parts, ',');
    }

    private List<String> getPatientAttributes(GenericProgramReportConfig config) {
        return config.getPatientAttributes() != null ? config.getPatientAttributes() : new ArrayList<String>();
    }

    private List<String> getProgramAttributes(GenericProgramReportConfig config) {
        return config.getProgramAttributes() != null ? config.getProgramAttributes() : new ArrayList<String>();
    }

    private List<String> getPatientAddresses(GenericProgramReportConfig config) {
        return config.getPatientAddresses() != null ? config.getPatientAddresses() : new ArrayList<String>();
    }

    private List<String> getProgramNamesToFilter(GenericProgramReportConfig config) {
        return config.getProgramNamesToFilter() != null ? config.getProgramNamesToFilter() : new ArrayList<String>();
    }
}
