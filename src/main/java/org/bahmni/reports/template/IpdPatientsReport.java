package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.IpdPatientsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "ipdPatients")
@UsingDatasource("openmrs")
public class IpdPatientsReport implements BaseReportTemplate<IpdPatientsConfig> {
    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<IpdPatientsConfig> reportConfig,
                                     String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {

        String patientAttributes = sqlStringListParameter(reportConfig.getConfig().getPatientAttributes());
        String conceptNames = sqlStringListParameter(reportConfig.getConfig().getConceptNames());

        StyleBuilder columnStyle = stl.style().setRightBorder(stl.pen1Point());

        TextColumnBuilder<String> patientIDColumn = col.column("Patient ID", "Patient ID", type.stringType()).setStyle(columnStyle);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle(columnStyle);
        TextColumnBuilder<Date> admissionDateColumn = col.column("Date of Admission", "Date of Admission", type.dateType()).setStyle(columnStyle);
        TextColumnBuilder<Date> dischargeDateColumn = col.column("Date of Discharge", "Date of Discharge", type.dateType()).setStyle(columnStyle);
        TextColumnBuilder<String> ageColumn = col.column("Age", "Age", type.stringType()).setStyle(columnStyle);
        TextColumnBuilder<String> diagnosisColumn = col.column("Diagnosis", "Diagnosis", type.stringType()).setStyle(columnStyle);



        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setTemplate(Templates.reportTemplate)
                .setShowColumnTitle(true)
                .setReportName(reportConfig.getName())
                .columns(admissionDateColumn, patientIDColumn, patientNameColumn, ageColumn)
                .pageFooter(Templates.footerComponent);

        addColumns(jasperReport, reportConfig.getConfig().getPatientAttributes(), columnStyle);
        addColumns(jasperReport, reportConfig.getConfig().getAddressAttributes(), columnStyle);

        jasperReport.columns(diagnosisColumn);
        jasperReport.columns(dischargeDateColumn);

        addColumns(jasperReport, reportConfig.getConfig().getConceptNames(), columnStyle);

        String sqlString = getSqlString(patientAttributes, conceptNames, startDate, endDate, getFilterColumn(reportConfig));
        Statement stmt = connection.createStatement();
        boolean hasMoreResultSets= stmt.execute(sqlString);
        while ( hasMoreResultSets || stmt.getUpdateCount() != -1 ) { //if there are any more queries to be processed
            if ( hasMoreResultSets ) {
                ResultSet rs = stmt.getResultSet();
                if(rs.isBeforeFirst()) {
                    jasperReport.setDataSource(rs);
                    return jasperReport;
                }
            }
            hasMoreResultSets = stmt.getMoreResults(); //true if it is a resultset
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

    private String getSqlString(String patientAttributes, String conceptNames, String startDate, String endDate, String filterColumn) {
        String sql = getFileContent("sql/ipdPatients.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("patientAttributes", patientAttributes);
        sqlTemplate.add("conceptNames", conceptNames);
        sqlTemplate.add("filterColumn", filterColumn);
        return sqlTemplate.render();
    }

    private String sqlStringListParameter(List<String> params) {
        return "\"" + StringUtils.join(params, "\", \"") + "\"";
    }

    private String getFilterColumn(Report<IpdPatientsConfig> reportConfig) {
        String filterBy = reportConfig.getConfig().getFilterBy();
        if("Date of Discharge".equals(filterBy)) {
            return "visit_attribute.date_changed";
        }
        return "visit_attribute.date_created";
    }
}
