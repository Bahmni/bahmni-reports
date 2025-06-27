package org.bahmni.reports.report;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisLineReportConfig;
import org.bahmni.reports.template.TSIntegrationDiagnosisLineReportTemplate;
import org.bahmni.reports.util.FileReaderUtil;
import org.bahmni.reports.util.SqlUtil;
import org.bahmni.webclients.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Use LENIENT strictness to avoid UnnecessaryStubbing exceptions
@RunWith(MockitoJUnitRunner.Silent.class)
public class TSIntegrationDiagnosisLineReportTest {

    @InjectMocks
    TSIntegrationDiagnosisLineReportTemplate tsIntegrationDiagnosisLineReportTemplate;

    @Mock
    Report<TSIntegrationDiagnosisLineReportConfig> mockReport;

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private JasperReportBuilder mockJasperReport;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockResultSetMetaData;

    @Mock
    private Properties mockTsProperties;

    // Using Mockito's MockedStatic instead of PowerMock for static mocking
    private MockedStatic<SqlUtil> sqlUtilMock;

    @Before
    public void setUp() {
        // Initialize static mock for SqlUtil class
        sqlUtilMock = Mockito.mockStatic(SqlUtil.class);
        tsIntegrationDiagnosisLineReportTemplate.setDescendantsUrlTemplate("dummyUrlTemplate");
    }

    @After
    public void tearDown() {
        // Important to close the static mock to avoid leaking resources
        if (sqlUtilMock != null) {
            sqlUtilMock.close();
        }
    }

    @Test
    public void shouldFetchTSDiagnosisLineReportTemplateWhenTSDiagnosisLineReportTypeIsInvoked() {
        TSIntegrationDiagnosisLineReportConfig tsIntegrationDiagnosisLineReportConfig = new TSIntegrationDiagnosisLineReportConfig();
        TSIntegrationDiagnosisLineReport tsIntegrationDiagnosisLineReport = new TSIntegrationDiagnosisLineReport();
        tsIntegrationDiagnosisLineReport.setConfig(tsIntegrationDiagnosisLineReportConfig);
        assertTrue(tsIntegrationDiagnosisLineReport.getTemplate(new BahmniReportsProperties()).getClass().isAssignableFrom(TSIntegrationDiagnosisLineReportTemplate.class));
    }

    @Test
    public void shouldIncludeTerminologyCodeColumnInJasperReport() throws Exception {
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        TSIntegrationDiagnosisLineReportConfig reportConfig = getMockTerminologyDiagnosisLineReportConfig(true, false, false);
        when(mockReport.getConfig()).thenReturn(addPatientAttributesToConfig(reportConfig, true));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);

        // Mock static method with Mockito instead of PowerMock
        sqlUtilMock.when(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString())).thenReturn(mockResultSet);

        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisLineReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(7)).addColumn(any());
    }

    @Test
    public void shouldExcludeTerminologyCodeColumnInJasperReport() throws Exception {
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        TSIntegrationDiagnosisLineReportConfig reportConfig = getMockTerminologyDiagnosisLineReportConfig(false, false, false);
        when(mockReport.getConfig()).thenReturn(addPatientAttributesToConfig(reportConfig, true));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);

        // Mock static method with Mockito instead of PowerMock
        sqlUtilMock.when(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString())).thenReturn(mockResultSet);

        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisLineReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(6)).addColumn(any());
    }

    @Test
    public void shouldIncludePatientAttributeColumnsInJasperReport() throws Exception {
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        TSIntegrationDiagnosisLineReportConfig reportConfig = getMockTerminologyDiagnosisLineReportConfig(false, false, false);
        when(mockReport.getConfig()).thenReturn(addPatientAttributesToConfig(reportConfig, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);

        // Mock static method with Mockito instead of PowerMock
        sqlUtilMock.when(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString())).thenReturn(mockResultSet);

        when(mockJasperReport.setDataSource((ResultSet) any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisLineReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(9)).addColumn(any());
    }

    @Test
    public void shouldIncludePatientAddressColumnsInJasperReport() throws Exception {
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        TSIntegrationDiagnosisLineReportConfig reportConfig = getMockTerminologyDiagnosisLineReportConfig(false, false, false);
        addPatientAttributesToConfig(reportConfig, true);
        when(mockReport.getConfig()).thenReturn(addPatientAddressesToConfig(reportConfig));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);

        // Mock static method with Mockito instead of PowerMock
        sqlUtilMock.when(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString())).thenReturn(mockResultSet);

        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisLineReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(8)).addColumn(any());
    }

    @Test
    public void shouldDisplayShortWhenConceptNameDisplayFormatEqualsShortNamePreferredInJasperReport() throws Exception {
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        TSIntegrationDiagnosisLineReportConfig reportConfig = getMockTerminologyDiagnosisLineReportConfig(false, true, false);
        addPatientAttributesToConfig(reportConfig, true);
        when(mockReport.getConfig()).thenReturn(addPatientAddressesToConfig(reportConfig));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);

        // Mock static method with Mockito instead of PowerMock
        sqlUtilMock.when(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString())).thenReturn(mockResultSet);

        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisLineReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        // Verify SqlUtil static method was called
        sqlUtilMock.verify(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString()));
    }

    @Test
    public void shouldDisplayFullySpecifiedWhenConceptNameDisplayFormatNotEqualsShortNamePreferredInJasperReport() throws Exception {
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        TSIntegrationDiagnosisLineReportConfig reportConfig = getMockTerminologyDiagnosisLineReportConfig(false, false, false);
        addPatientAttributesToConfig(reportConfig, true);
        when(mockReport.getConfig()).thenReturn(addPatientAddressesToConfig(reportConfig));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);

        // Mock static method with Mockito instead of PowerMock
        sqlUtilMock.when(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString())).thenReturn(mockResultSet);

        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisLineReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        // Verify SqlUtil static method was called
        sqlUtilMock.verify(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString()));
    }

    @Test
    public void shouldIncludeIcd10TerminologyCodeColumnInJasperReport() throws Exception {
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        TSIntegrationDiagnosisLineReportConfig reportConfig = getMockTerminologyDiagnosisLineReportConfig(true, false, false);
        reportConfig.setConceptSource("ICD-10-WHO");
        // Initialize patient attributes to prevent NPE
        reportConfig.setPatientAttributes(new ArrayList<>());
        when(mockReport.getConfig()).thenReturn(reportConfig);

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);

        // Mock static method with Mockito instead of PowerMock
        sqlUtilMock.when(() -> SqlUtil.executeSqlWithStoredProc(any(), anyString())).thenReturn(mockResultSet);

        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisLineReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(7)).addColumn(any());
    }

    private TSIntegrationDiagnosisLineReportConfig getMockTerminologyDiagnosisLineReportConfig(boolean displayTerminologyCodeFlag, boolean shortNamePreferredFlag, boolean icd10ExtensionFlag) {
        TSIntegrationDiagnosisLineReportConfig tsIntegrationDiagnosisLineReportConfig = new TSIntegrationDiagnosisLineReportConfig();
        tsIntegrationDiagnosisLineReportConfig.setDisplayTerminologyCode(displayTerminologyCodeFlag);
        tsIntegrationDiagnosisLineReportConfig.setTerminologyParentCode("dummyCode");
        if (shortNamePreferredFlag)
            tsIntegrationDiagnosisLineReportConfig.setConceptNameDisplayFormat("shortNamePreferred");
        if (icd10ExtensionFlag)
            tsIntegrationDiagnosisLineReportConfig.setExtensions(Arrays.asList("org.bahmni.reports.extensions.sample.SampleResultSetExtension"));
        return tsIntegrationDiagnosisLineReportConfig;
    }

    private TSIntegrationDiagnosisLineReportConfig addPatientAttributesToConfig(TSIntegrationDiagnosisLineReportConfig tsIntegrationDiagnosisLineReportConfig, boolean isEmpty) {
        List<String> patientAttributeList = isEmpty ? new ArrayList<>() : Arrays.asList("education", "primaryContact", "secondaryContact");
        tsIntegrationDiagnosisLineReportConfig.setPatientAttributes(patientAttributeList);
        return tsIntegrationDiagnosisLineReportConfig;
    }

    private TSIntegrationDiagnosisLineReportConfig addPatientAddressesToConfig(TSIntegrationDiagnosisLineReportConfig tsIntegrationDiagnosisLineReportConfig) {
        List<String> patientAddressesList = Arrays.asList("city_village", "address3");
        tsIntegrationDiagnosisLineReportConfig.setPatientAddresses(patientAddressesList);
        return tsIntegrationDiagnosisLineReportConfig;
    }

    private String getMockTerminologyDescendants() {
        return FileReaderUtil.getFileContent("terminologyServices/descendantCodes.json");
    }

}