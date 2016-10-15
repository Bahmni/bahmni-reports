package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.IpdPatientsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.PatientAttributesHelper;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class IpdPatientsReportTemplate extends BaseReportTemplate<IpdPatientsConfig> {
    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<IpdPatientsConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);

        PatientAttributesHelper patientAttributesHelper = new PatientAttributesHelper(report.getConfig().getPatientAttributes());

        String conceptNames = prepareForSqlClause(report.getConfig().getConceptNames());

        TextColumnBuilder<String> patientIDColumn = col.column("Patient ID", "Patient ID", type.stringType()).setStyle(minimalColumnStyle);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle
                (minimalColumnStyle);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle);
        TextColumnBuilder<Date> admissionDateColumn = col.column("Date of Admission", "Date of Admission", type.dateType()).setStyle
                (minimalColumnStyle);
        TextColumnBuilder<Date> dischargeDateColumn = col.column("Date of Discharge", "Date of Discharge", type.dateType()).setStyle
                (minimalColumnStyle);
        TextColumnBuilder<String> ageColumn = col.column("Age", "Age", type.stringType()).setStyle(minimalColumnStyle);
        TextColumnBuilder<String> diagnosisColumn = col.column("Diagnosis", "Diagnosis", type.stringType()).setStyle(minimalColumnStyle);
        TextColumnBuilder<String> visitType = col.column("Visit Type", "Visit Type", type.stringType()).setStyle(minimalColumnStyle);

        jasperReport.setShowColumnTitle(true)
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
                .columns(admissionDateColumn, visitType, patientIDColumn, patientNameColumn, genderColumn, ageColumn);

        addColumns(jasperReport, report.getConfig().getPatientAttributes(), minimalColumnStyle);
        addColumns(jasperReport, report.getConfig().getAddressAttributes(), minimalColumnStyle);

        jasperReport.columns(diagnosisColumn);
        jasperReport.columns(dischargeDateColumn);

        addColumns(jasperReport, report.getConfig().getConceptNames(), minimalColumnStyle);

        String sqlString = getSqlString(report.getConfig(),patientAttributesHelper, conceptNames, startDate, endDate, getFilterColumn(report));

        JasperReportBuilder jasperReportBuilder = SqlUtil.executeReportWithStoredProc(jasperReport, connection, sqlString);
        return new BahmniReportBuilder(jasperReportBuilder);
    }

    private void addColumns(JasperReportBuilder jasperReport, List<String> attributes, StyleBuilder columnStyle) {
        for (String attribute : attributes) {
            TextColumnBuilder<String> attributeColumn = col.column(attribute, attribute, type.stringType())
                    .setStyle(columnStyle);
            jasperReport.addColumn(attributeColumn);
        }
    }

    private String getSqlString(IpdPatientsConfig reportConfig,PatientAttributesHelper patientAttributesHelper,String conceptNames, String startDate, String endDate, String filterColumn) {
        String locationTagNames = SqlUtil.toEscapedCommaSeparatedSqlString(reportConfig.getLocationTagNames());
        String sql = getFileContent("sql/ipdPatients.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("conceptNames", conceptNames);
        sqlTemplate.add("filterColumn", filterColumn);
        sqlTemplate.add("patientAttributesFromClause",patientAttributesHelper.getFromClause());
        sqlTemplate.add("patientAttributeSql",patientAttributesHelper.getSql());
        if (StringUtils.isNotBlank(locationTagNames)) {
            String countOnlyTaggedLocationsJoin = String.format("INNER JOIN " +
                    "(SELECT DISTINCT location_id " +
                    "FROM location_tag_map INNER JOIN location_tag ON location_tag_map.location_tag_id = location_tag.location_tag_id " +
                    " AND location_tag.name IN (%s)) locations ON locations.location_id = e.location_id", locationTagNames);
            sqlTemplate.add("countOnlyTaggedLocationsJoin", countOnlyTaggedLocationsJoin);
        } else {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", "");
        }
        return sqlTemplate.render();
    }

    private String prepareForSqlClause(List<String> params) {
        return "\"" + StringUtils.join(params, "\", \"") + "\"";
    }

    private String getFilterColumn(Report<IpdPatientsConfig> reportConfig) {
        String filterBy = reportConfig.getConfig().getFilterBy();
        if ("Date of Discharge".equals(filterBy)) {
            return "visit_attribute.date_changed";
        }
        return "visit_attribute.date_created";
    }
}
