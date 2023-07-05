package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.extension.icd10.ResultSetExtension;
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

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
    private static final Logger logger = LogManager.getLogger(TSIntegrationDiagnosisLineReportTemplate.class);
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
        jasperReport.addColumn(col.column(PATIENT_ID_COLUMN_NAME, PATIENT_ID_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(PATIENT_NAME_COLUMN_NAME, PATIENT_NAME_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(GENDER_COLUMN_NAME, GENDER_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
        createAndAddPatientAddressColumns(jasperReport, report.getConfig());
        jasperReport.addColumn(col.column(PATIENT_DATE_OF_BIRTH_COLUMN_NAME, PATIENT_DATE_OF_BIRTH_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        jasperReport.addColumn(col.column(DIAGNOSIS_COLUMN_NAME, DIAGNOSIS_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        if (report.getConfig().isDisplayTerminologyCode()) {
            String terminologyConfigColumnName = report.getConfig().getTerminologyColumnName();
            String terminologyColumnName = StringUtils.isNotBlank(terminologyConfigColumnName) ? terminologyConfigColumnName : TERMINOLOGY_COLUMN_NAME;
            jasperReport.addColumn(col.column(terminologyColumnName, TERMINOLOGY_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        }
        jasperReport.addColumn(col.column(DATE_AND_TIME_COLUMN_NAME, DATE_AND_TIME_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        ResultSet resultSet = getResultSet(sql, report.getConfig().getTsConceptSource(), startDate, endDate, tempTableName, report.getConfig(), connection);

        String extensionClass = report.getConfig().getExtensionClass();
        if(StringUtils.isNotBlank(extensionClass)) {
            Collection<Map<String, String>> rawCollection = convertResultSetToCollection(resultSet);
            Collection<Map<String, ?>> enrichCollection = enrichUsingReflection(rawCollection, extensionClass, jasperReport);
            jasperReport.setDataSource(new JRMapCollectionDataSource(enrichCollection));
        }else{
            jasperReport.setDataSource(resultSet);
        }
        return new BahmniReportBuilder(jasperReport);
    }

    private Collection<Map<String, ?>> enrichUsingReflection(Collection<Map<String, String>> rawCollection, String extensionClassStr, JasperReportBuilder jasperReport) {
        Collection<Map<String, ?>> enrichCollection = null;
        try {
            Class<?> extensionClass = Class.forName(extensionClassStr);
            Constructor constructor = extensionClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            ResultSetExtension wrapper = (ResultSetExtension) constructor.newInstance();
            enrichCollection = wrapper.enrich(rawCollection);
            jasperReport.addColumn(col.column(wrapper.getColumnName(), wrapper.getColumnName(), type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        } catch (Exception ex) {
            logger.error(String.format("Error caused during reflection in enrichUsingReflection method: %s", ex.getMessage()));
            throw new RuntimeException();
        }
        return enrichCollection;
    }

    public List<Map<String, String>> convertResultSetToCollection(ResultSet resultSet) throws SQLException {
        List<Map<String, String>> resulSetListOfMap = new ArrayList<>();
        if (resultSet != null) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                Map<String, String> resulSetMap = new HashMap<>();
                for (int colNextIndex = 1; colNextIndex <= columnCount; colNextIndex++) {
                    resulSetMap.put(resultSet.getMetaData().getColumnLabel(colNextIndex), resultSet.getString(colNextIndex));
                }
                resulSetListOfMap.add(resulSetMap);
            }
        }
        return resulSetListOfMap;
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
