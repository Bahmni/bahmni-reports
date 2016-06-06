package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.GenericVisitReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class GenericVisitReportTemplate extends BaseReportTemplate<GenericVisitReportConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<GenericVisitReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);

        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        createAndAddMandatoryColumns(jasperReport);
        if (report.getConfig() != null) {
            createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
            createAndAddPatientAddressColumns(jasperReport, report.getConfig());
            createAndAddVisitAttributeColumns(jasperReport, report.getConfig());
            createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
        }

        String formattedSql = getFormattedSql(report.getConfig(), startDate, endDate);

        return SqlUtil.executeReportWithStoredProc(jasperReport, connection, formattedSql);
    }

    private String getFormattedSql(GenericVisitReportConfig config, String startDate, String endDate) {
        String sql = getFileContent("sql/genericVisitReport.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (config != null) {
            sqlTemplate.add("patientAttributes", constructPatientAttributeNamesString(getPatientAttributes(config)));
            sqlTemplate.add("patientAddresses", constructPatientAddresses(getPatientAddresses(config)));
            sqlTemplate.add("visitAttributes", constructVisitAttributeNamesString(getVisitAttributes(config)));
            sqlTemplate.add("visitTypesToFilter", constructVisitTypesString(getVisitTypesToFilter(config)));
        }
        sqlTemplate.add("applyDateRangeFor", getDateRangeFor(config));
        return sqlTemplate.render();
    }

    private void createAndAddVisitAttributeColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        for (String visitAttribute : getVisitAttributes(config)) {
            TextColumnBuilder<String> column = col.column(visitAttribute, visitAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private void createAndAddPatientAddressColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        for (String address : getPatientAddresses(config)) {
            TextColumnBuilder<String> column = col.column(address, address, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private void createAndAddPatientAttributeColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        for (String patientAttribute : getPatientAttributes(config)) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
            jasperReport.addColumn(column);
        }
    }

    private void createAndAddDataAnalysisColumns(JasperReportBuilder jasperReport, GenericVisitReportConfig config) {
        if (config.isForDataAnalysis()) {
            TextColumnBuilder<Integer> patientIdColumn = col.column("Patient Id", "Patient Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            TextColumnBuilder<Integer> visitIdColumn = col.column("Visit Id", "Visit Id", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER).setPattern("#");
            jasperReport.addColumn(patientIdColumn);
            jasperReport.addColumn(visitIdColumn);
        }
    }

    private void createAndAddMandatoryColumns(JasperReportBuilder jasperReport) {
        TextColumnBuilder<String> patientIdentifierColumn = col.column("Patient Identifier", "Patient Identifier", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> ageColumn = col.column("Age", "Age", type.integerType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> birthdateColumn = col.column("Birthdate", "Birthdate", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> patientCreatedDateColumn = col.column("Patient Created Date", "Patient Created Date", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<String> visitTypeColumn = col.column("Visit Type", "Visit Type", type.stringType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> dateStartedColumn = col.column("Date started", "Date started", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> dateStoppedColumn = col.column("Date stopped", "Date stopped", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> dateOfAdmission = col.column("Date Of Admission", "Date Of Admission", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Date> dateOfDischarge = col.column("Date Of Discharge", "Date Of Discharge", type.dateType()).setStyle(minimalColumnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);

        jasperReport.columns(patientIdentifierColumn, patientNameColumn, ageColumn, birthdateColumn, genderColumn, patientCreatedDateColumn, visitTypeColumn, dateStartedColumn, dateStoppedColumn, dateOfAdmission, dateOfDischarge);
    }

    private String constructVisitTypesString(List<String> visitTypesToFilter) {
        List<String> parts = new ArrayList<>();
        for (String visitType : visitTypesToFilter) {
            parts.add("\"" + visitType + "\"");
        }
        return StringUtils.join(parts, ',');
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

    private String constructVisitAttributeNamesString(List<String> visitAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(vat.name = \\'%s\\', va.value_reference, NULL))) AS \\'%s\\'";

        for (String visitAttribute : visitAttributes) {
            parts.add(String.format(helperString, visitAttribute, visitAttribute));
        }

        return StringUtils.join(parts, ", ");
    }

    private String constructPatientAttributeNamesString(List<String> patientAttributes) {
        List<String> parts = new ArrayList<>();
        String helperString = "GROUP_CONCAT(DISTINCT(IF(pat.name = \\'%s\\', IF(pat.format = \\'org.openmrs.Concept\\',coalesce(scn.name, fscn.name),pa.value), NULL))) AS \\'%s\\'";

        for (String patientAttribute : patientAttributes) {
            parts.add(String.format(helperString, patientAttribute, patientAttribute));
        }

        return StringUtils.join(parts, ", ");
    }

    private List<String> getVisitTypesToFilter(GenericVisitReportConfig config) {
        return config.getVisitTypesToFilter() != null ? config.getVisitTypesToFilter() : new ArrayList<String>();
    }

    private List<String> getPatientAttributes(GenericVisitReportConfig config) {
        return config.getPatientAttributes() != null ? config.getPatientAttributes() : new ArrayList<String>();
    }

    private List<String> getVisitAttributes(GenericVisitReportConfig config) {
        return config.getVisitAttributes() != null ? config.getVisitAttributes() : new ArrayList<String>();
    }

    private List<String> getPatientAddresses(GenericVisitReportConfig config) {
        return config.getPatientAddresses() != null ? config.getPatientAddresses() : new ArrayList<String>();
    }

    private String getDateRangeFor(GenericVisitReportConfig config) {
        if (config != null && "visitStopDate".equals(config.getApplyDateRangeFor())) {
            return "v.date_stopped";
        }
        return "v.date_started";
    }
}
