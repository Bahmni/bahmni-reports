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
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.PatientAttributesHelper;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class IpdPatientsReportTemplate extends BaseReportTemplate<IpdPatientsConfig> {
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<IpdPatientsConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        PatientAttributesHelper patientAttributesHelper = new PatientAttributesHelper(report.getConfig().getPatientAttributes());

        String conceptNames = prepareForSqlClause(report.getConfig().getConceptNames());

        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> patientIDColumn = col.column("Patient ID", "Patient ID", type.stringType()).setStyle(columnStyle);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(columnStyle);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(columnStyle);
        TextColumnBuilder<Date> admissionDateColumn = col.column("Date of Admission", "Date of Admission", type.dateType()).setStyle(columnStyle);
        TextColumnBuilder<Date> dischargeDateColumn = col.column("Date of Discharge", "Date of Discharge", type.dateType()).setStyle(columnStyle);
        TextColumnBuilder<String> ageColumn = col.column("Age", "Age", type.stringType()).setStyle(columnStyle);
        TextColumnBuilder<String> diagnosisColumn = col.column("Diagnosis", "Diagnosis", type.stringType()).setStyle(columnStyle);
        TextColumnBuilder<String> visitType = col.column("Visit Type", "Visit Type", type.stringType()).setStyle(columnStyle);

        jasperReport.setShowColumnTitle(true)
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
                .columns(admissionDateColumn, visitType, patientIDColumn, patientNameColumn, genderColumn, ageColumn);

        addColumns(jasperReport, report.getConfig().getPatientAttributes(), columnStyle);
        addColumns(jasperReport, report.getConfig().getAddressAttributes(), columnStyle);

        jasperReport.columns(diagnosisColumn);
        jasperReport.columns(dischargeDateColumn);

        addColumns(jasperReport, report.getConfig().getConceptNames(), columnStyle);

        String sqlString = getSqlString(patientAttributesHelper, conceptNames, startDate, endDate, getFilterColumn(report));
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            boolean hasMoreResultSets = stmt.execute(sqlString);
            while (hasMoreResultSets || stmt.getUpdateCount() != -1) { //if there are any more queries to be processed
                if (hasMoreResultSets) {
                    ResultSet rs = stmt.getResultSet();
                    if (rs.isBeforeFirst()) {
                        jasperReport.setDataSource(rs);
                        return jasperReport;
                    }
                }
                hasMoreResultSets = stmt.getMoreResults(); //true if it is a resultset
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jasperReport;
    }

    private void addColumns(JasperReportBuilder jasperReport, List<String> attributes, StyleBuilder columnStyle) {
        for (String attribute : attributes) {
            TextColumnBuilder<String> attributeColumn = col.column(attribute, attribute, type.stringType())
                    .setStyle(columnStyle);
            jasperReport.addColumn(attributeColumn);
        }
    }

    private String getSqlString(PatientAttributesHelper patientAttributesHelper,String conceptNames, String startDate, String endDate, String filterColumn) {
        String sql = getFileContent("sql/ipdPatients.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("conceptNames", conceptNames);
        sqlTemplate.add("filterColumn", filterColumn);
        sqlTemplate.add("patientAttributesFromClause",patientAttributesHelper.getFromClause());
        sqlTemplate.add("patientAttributeSql",patientAttributesHelper.getSql());
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
