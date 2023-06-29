package org.bahmni.reports.template;


import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.icd10.ICD10Decorator;
import org.bahmni.reports.icd10.bean.ICDMap;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisLineReportConfig;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.PatientAttributesHelper;
import org.bahmni.reports.util.SqlUtil;
import org.bahmni.webclients.HttpClient;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.columnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;


@UsingDatasource("openmrs")
public class TSIntegrationDiagnosisLineReportTemplate extends BaseReportTemplate<TSIntegrationDiagnosisLineReportConfig> implements TSIntegrationDiagnosisService {
    public static final String DIAGNOSIS_COLUMN_NAME = "Diagnosis";
    public static final String TERMINOLOGY_COLUMN_NAME = "Terminology Code";
    public static final String PATIENT_ID_COLUMN_NAME = "Patient Id";
    public static final String PATIENT_NAME_COLUMN_NAME = "Patient Name";
    public static final String PATIENT_DATE_OF_BIRTH_COLUMN_NAME = "Date of Birth";
    public static final String GENDER_COLUMN_NAME = "Gender";
    public static final String DATE_AND_TIME_COLUMN_NAME = "Date & Time of Diagnosis";
    public static final String SHORT_DISPLAY_FORMAT = "SHORT";
    public static final String FULLY_SPECIFIED_DISPLAY_FORMAT = "FULLY_SPECIFIED";
    private HttpClient httpClient;
    private Properties tsProperties;
    private String descendantsUrlTemplate;

    public TSIntegrationDiagnosisLineReportTemplate(HttpClient httpClient, Properties tsProperties, String descendantsUrlTemplate) {
        super();
        this.httpClient = httpClient;
        this.tsProperties = tsProperties;
        this.descendantsUrlTemplate = descendantsUrlTemplate;
    }

    public void setDescendantsUrlTemplate(String descendantsUrlTemplate) {
        this.descendantsUrlTemplate = descendantsUrlTemplate;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<TSIntegrationDiagnosisLineReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException, InvalidConfigurationException {
        String tempTableName = "tmpCodes_" + System.nanoTime();
        loadTempTable(connection, tempTableName, report.getConfig().getTerminologyParentCode(), tsProperties, descendantsUrlTemplate, httpClient);
        String sql = getFileContent("sql/tsIntegrationDiagnosisLineReport.sql");

        CommonComponents.addTo(jasperReport, report, pageType);
        if (report.getConfig().isDisplayTerminologyCode()) {
            String terminologyConfigColumnName = report.getConfig().getTerminologyColumnName();
            String terminologyColumnName = StringUtils.isNotBlank(terminologyConfigColumnName) ? terminologyConfigColumnName : TERMINOLOGY_COLUMN_NAME;
            jasperReport.addColumn(col.column(terminologyColumnName, "snomedCode", type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        }
        jasperReport.addColumn(col.column(PATIENT_DATE_OF_BIRTH_COLUMN_NAME, "age", type.integerType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(GENDER_COLUMN_NAME, "gender", type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column("ICD Codes", "icdCodes", type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        /*
        jasperReport.addColumn(col.column(PATIENT_ID_COLUMN_NAME, PATIENT_ID_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(PATIENT_NAME_COLUMN_NAME, PATIENT_NAME_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(GENDER_COLUMN_NAME, GENDER_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
        createAndAddPatientAddressColumns(jasperReport, report.getConfig());
        jasperReport.addColumn(col.column(PATIENT_DATE_OF_BIRTH_COLUMN_NAME, PATIENT_DATE_OF_BIRTH_COLUMN_NAME, type.dateType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(DIAGNOSIS_COLUMN_NAME, DIAGNOSIS_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        if (report.getConfig().isDisplayTerminologyCode()) {
            String terminologyConfigColumnName = report.getConfig().getTerminologyColumnName();
            String terminologyColumnName = StringUtils.isNotBlank(terminologyConfigColumnName) ? terminologyConfigColumnName : TERMINOLOGY_COLUMN_NAME;
            jasperReport.addColumn(col.column(terminologyColumnName, TERMINOLOGY_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        }
        jasperReport.addColumn(col.column(DATE_AND_TIME_COLUMN_NAME, DATE_AND_TIME_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column("ICD10 Code", "ICD10 Code", type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        */
        ResultSet resultSet = getResultSet(sql, report.getConfig().getTsConceptSource(), startDate, endDate, tempTableName, report.getConfig(), connection);
        try {
            Collection<ICDMap>  collection = new ICD10Decorator().enrich(resultSet);
            jasperReport.setDataSource(collection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new BahmniReportBuilder(jasperReport);
    }


    private ResultSet getResultSet(String templateSql, String conceptSourceCode, String startDate, String endDate, String tempTableName, TSIntegrationDiagnosisLineReportConfig config, Connection connection) throws SQLException, InvalidConfigurationException {
        ST sqlTemplate = new ST(templateSql, '#', '#');
        sqlTemplate.add("conceptSourceCode", conceptSourceCode);
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("tempTable", tempTableName);
        PatientAttributesHelper patientAttributesHelper = new PatientAttributesHelper(config.getPatientAttributes());
        sqlTemplate.add("patientAttributesFromClause", patientAttributesHelper.getFromClause());
        sqlTemplate.add("patientAttributeSql", patientAttributesHelper.getSql());
        sqlTemplate.add("patientAddresses", constructPatientAddressesToDisplay(config));
        sqlTemplate.add("conceptNameDisplayFormat", "shortNamePreferred".equals(config.getConceptNameDisplayFormat()) ? SHORT_DISPLAY_FORMAT : FULLY_SPECIFIED_DISPLAY_FORMAT);
        return SqlUtil.executeSqlWithStoredProc(connection, sqlTemplate.render());
    }


}
