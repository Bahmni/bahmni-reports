package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ObsCannedReportTemplateConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class ObsCannedReportTemplate extends BaseReportTemplate<ObsCannedReportTemplateConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<ObsCannedReportTemplateConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);
        ObsCannedReportTemplateConfig obsCannedReportTemplateConfig = report.getConfig();

        List<String> patientAttributes = obsCannedReportTemplateConfig.getPatientAttributes();
        if (patientAttributes == null) {
            patientAttributes = new ArrayList<>();
        }
        List<String> addressAttributes = obsCannedReportTemplateConfig.getAddressAttributes();
        if (addressAttributes == null) {
            addressAttributes = new ArrayList<>();
        }

        List<String> conceptDetails = obsCannedReportTemplateConfig.getConceptNames();
        if (conceptDetails == null) {
            conceptDetails = new ArrayList<>();
        }
        List<String> viConceptDetails = obsCannedReportTemplateConfig.getVisitIndependentConcept();
        if (viConceptDetails == null) {
            viConceptDetails = new ArrayList<>();
        }
        String conceptNameInClause =  constructInClause(conceptDetails);
        String patientAttributesInClause = constructInClause(patientAttributes);
        String addressAttributesInClause = constructInClause(addressAttributes);
        String sql = getFormattedSql(getFileContent("sql/obsCannedReport.sql"), obsCannedReportTemplateConfig, conceptNameInClause,
                patientAttributesInClause, startDate, endDate,addressAttributesInClause );buildColumns(jasperReport, patientAttributes, conceptDetails,viConceptDetails, addressAttributes);

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);
        JasperReportBuilder jasperReportBuilder = SqlUtil.executeReportWithStoredProc(jasperReport, connection, sql);
        return new BahmniReportBuilder(jasperReportBuilder);
    }

    private void buildColumns(JasperReportBuilder jasperReport, List<String> patientAttributes, List<String> conceptNames,List<String> viConceptNames, List<String> addressAttributes) {
        TextColumnBuilder<String> patientColumn = col.column("Patient ID", "identifier", type.stringType());
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "patient_name", type.stringType());
        TextColumnBuilder<String> patientGenderColumn = col.column("Gender", "gender", type.stringType());
        TextColumnBuilder<String> patientAgeColumn = col.column("Age", "age", type.stringType());
        TextColumnBuilder<String> visit = col.column("Visit Id", "visit_id", type.stringType());


        jasperReport.columns(patientColumn, patientNameColumn, patientGenderColumn, patientAgeColumn,visit);

        for (String patientAttribute : patientAttributes) {
            TextColumnBuilder<String> column = col.column(patientAttribute, patientAttribute, type.stringType());
            jasperReport.addColumn(column);
        }
        for (String addressAttribute : addressAttributes) {
            TextColumnBuilder<String> column = col.column(addressAttribute, addressAttribute, type.stringType());
            jasperReport.addColumn(column);
        }
        for (String conceptName : conceptNames) {
            TextColumnBuilder<String> column1 = col.column(conceptName, conceptName, type.stringType());
            jasperReport.addColumn(column1);
        }
        for (String viConceptName : viConceptNames) {
            TextColumnBuilder<String> column1 = col.column(viConceptName, "latest_"+viConceptName, type.stringType());
            jasperReport.addColumn(column1);
        }
    }

    private String getFormattedSql(String formattedSql, ObsCannedReportTemplateConfig reportConfig, String conceptNameInClause, String
            patientAttributeInClause, String startDate, String endDate, String addressAttributesInClause) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("conceptNameInClause", conceptNameInClause);
        sqlTemplate.add("conceptNameInClauseEscapeQuote", getInClauseWithEscapeQuote(conceptNameInClause));
        sqlTemplate.add("patientAttributesInClause", patientAttributeInClause);
        sqlTemplate.add("patientAttributesInClauseEscapeQuote", getInClauseWithEscapeQuote(patientAttributeInClause));
        sqlTemplate.add("addressAttributesInClause", addressAttributesInClause);
        sqlTemplate.add("templateName", reportConfig.getTemplateName());
        sqlTemplate.add("applyDateRangeFor", reportConfig.getApplyDateRangeFor());
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("enrolledProgram", reportConfig.getEnrolledProgram());
        sqlTemplate.add("showObsOnlyForProgramDuration", reportConfig.getShowObsOnlyForProgramDuration());
        sqlTemplate.add("visitIndependentConceptInClause", constructInClause(reportConfig.getVisitIndependentConcept()));
        sqlTemplate.add("visitIndependentConceptInClauseEscaped", getInClauseWithEscapeQuote(constructInClause(reportConfig.getVisitIndependentConcept())));
        sqlTemplate.add("conceptSourceName", reportConfig.getConceptSource());

        return sqlTemplate.render();
    }

    private String constructInClause(List<String> parameters) {
        List<String> convertedList = new ArrayList<>();
        if (parameters.isEmpty()) {
            return "''";
        }
        for (String parameter : parameters) {
            convertedList.add("'" + parameter + "'");
        }
        return StringUtils.join(convertedList, ",");
    }

    private String getInClauseWithEscapeQuote(String inclause) {
        return inclause.replace("'", "\\'");
    }

}
