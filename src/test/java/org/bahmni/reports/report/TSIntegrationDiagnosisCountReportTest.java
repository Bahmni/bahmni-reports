package org.bahmni.reports.report;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisCountReportConfig;
import org.bahmni.reports.template.TSIntegrationDiagnosisCountReportTemplate;
import org.bahmni.reports.util.FileReaderUtil;
import org.bahmni.webclients.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.quality.Strictness;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Use LENIENT strictness to avoid UnnecessaryStubbing exceptions
@RunWith(MockitoJUnitRunner.Silent.class)
public class TSIntegrationDiagnosisCountReportTest {
    @InjectMocks
    TSIntegrationDiagnosisCountReportTemplate tsIntegrationDiagnosisCountReportTemplate;

    @Mock
    Report<TSIntegrationDiagnosisCountReportConfig> mockReport;

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
    private Properties mockTsProperties;

    @Before
    public void setUp() {
        tsIntegrationDiagnosisCountReportTemplate.setDescendantsUrlTemplate("dummyUrlTemplate");

        // Critical: Set up the Statement mock to avoid NullPointerException
        try {
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.execute(anyString())).thenReturn(false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up mocks", e);
        }
    }

    @Test
    public void shouldFetchTSDiagnosisCountReportTemplateWhenTSDiagnosisCountReportTypeIsInvoked() {
        TSIntegrationDiagnosisCountReportConfig tsIntegrationDiagnosisCountReportConfig = new TSIntegrationDiagnosisCountReportConfig();
        TSIntegrationDiagnosisCountReport tsIntegrationDiagnosisCountReport = new TSIntegrationDiagnosisCountReport();
        tsIntegrationDiagnosisCountReport.setConfig(tsIntegrationDiagnosisCountReportConfig);
        assertTrue(tsIntegrationDiagnosisCountReport.getTemplate(new BahmniReportsProperties()).getClass().isAssignableFrom(TSIntegrationDiagnosisCountReportTemplate.class));
    }

    @Test
    public void shouldProcessTerminologyDescendantsWithPagination() throws Exception {
        // Only setup mocks that are actually used in this test
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(true, true, false));
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");

        // For methods that need to be chained, we still need to set them up
        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        // Verify only what's relevant to this test
        verify(mockPreparedStatement, times(2)).setString(eq(1), anyString());
        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement, times(1)).executeBatch();
    }

    @Test
    public void shouldIncludeBothTerminologyCodeAndGenderGroupColumnsInJasperReport() throws Exception {
        // Setup minimal mocks needed for this test
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(true, true, false));
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");

        // Required for chaining
        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(7)).addColumn(any());
    }

    @Test
    public void shouldIncludeTerminologyCodeAndExcludeGenderGroupColumnsInJasperReport() throws Exception {
        // Setup minimal mocks needed for this test
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(true, false, false));
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");

        // Required for chaining
        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(3)).addColumn(any());
    }

    @Test
    public void shouldExcludeTerminologyCodeAndIncludeGenderGroupColumnsInJasperReport() throws Exception {
        // Setup minimal mocks needed for this test
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(false, true, false));
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");

        // Required for chaining
        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(6)).addColumn(any());
    }

    @Test
    public void shouldExcludeBothTerminologyCodeAndGenderGroupColumnsInJasperReport() throws Exception {
        // Setup minimal mocks needed for this test
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(false, false, false));
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");

        // Required for chaining
        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(2)).addColumn(any());
    }

    @Test
    public void shouldDisplayShortWhenConceptNameDisplayFormatEqualsShortNamePreferredInJasperReport() throws Exception {
        // Setup minimal mocks needed for this test
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(false, false, true));
        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");

        // Required for chaining
        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(1)).setDataSource(contains("AND cn.concept_name_type = 'SHORT'"), any());
    }

    @Test
    public void shouldDisplayFullySpecifiedWhenConceptNameDisplayFormatNotEqualsShortNamePreferredInJasperReport() throws Exception {
        // Setup minimal mocks needed for this test
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(false, false, false));
        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());
        when(mockTsProperties.getProperty("ts.defaultPageSize")).thenReturn("10000");

        // Required for chaining
        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(1)).setDataSource(contains("AND cn.concept_name_type = 'FULLY_SPECIFIED'"), any());
    }

    private TSIntegrationDiagnosisCountReportConfig getMockTerminologyDiagnosisCountReportConfig(boolean displayTerminologyCodeFlag, boolean displayGenderGroupFlag, boolean shortNamePreferredFlag) {
        TSIntegrationDiagnosisCountReportConfig tsIntegrationDiagnosisCountReportConfig = new TSIntegrationDiagnosisCountReportConfig();
        tsIntegrationDiagnosisCountReportConfig.setDisplayTerminologyCode(displayTerminologyCodeFlag);
        tsIntegrationDiagnosisCountReportConfig.setDisplayGenderGroup(displayGenderGroupFlag);
        tsIntegrationDiagnosisCountReportConfig.setTerminologyParentCode("dummyCode");
        if (shortNamePreferredFlag)
            tsIntegrationDiagnosisCountReportConfig.setConceptNameDisplayFormat("shortNamePreferred");
        return tsIntegrationDiagnosisCountReportConfig;
    }

    private String getMockTerminologyDescendants() {
        return FileReaderUtil.getFileContent("terminologyServices/descendantCodes.json");
    }
}
