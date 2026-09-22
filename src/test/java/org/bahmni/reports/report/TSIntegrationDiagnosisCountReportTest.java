package org.bahmni.reports.report;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisCountReportConfig;
import org.bahmni.reports.template.TSIntegrationDiagnosisCountReportTemplate;
import org.bahmni.reports.util.FileReaderUtil;
import org.bahmni.webclients.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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
        // descendantCodes.json reports total=2 with 2 codes, and the mocked client returns it for
        // every page. Page size 1 therefore drives two loop passes (offset 0, then 1, stopping at
        // 2), which is what makes the configured page size observable at all: the hardcoded
        // fallback is 20, so any value >= 2 is indistinguishable from not reading the property.
        when(mockTsProperties.getProperty("terminologyServer.defaultPageSize")).thenReturn("1");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(true, true, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        // The configured page size, not the fallback, is what the loop ran on.
        verify(mockTsProperties).getProperty("terminologyServer.defaultPageSize");
        // Two pages, two descendant codes inserted per page.
        verify(mockPreparedStatement, times(4)).setString(eq(1), anyString());
        verify(mockPreparedStatement, times(4)).addBatch();
        // One batch executed per page: ceil(total 2 / pageSize 1).
        verify(mockPreparedStatement, times(2)).executeBatch();
    }

    @Test
    public void shouldIncludeBothTerminologyCodeAndGenderGroupColumnsInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(true, true, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(7)).addColumn(any());
    }

    @Test
    public void shouldIncludeTerminologyCodeAndExcludeGenderGroupColumnsInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(true, false, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(3)).addColumn(any());
    }

    @Test
    public void shouldExcludeTerminologyCodeAndIncludeGenderGroupColumnsInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(false, true, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(6)).addColumn(any());
    }

    @Test
    public void shouldExcludeBothTerminologyCodeAndGenderGroupColumnsInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(false, false, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(2)).addColumn(any());
    }

    @Test
    public void shouldDisplayShortWhenConceptNameDisplayFormatEqualsShortNamePreferredInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(false, false, true));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);
        verify(mockJasperReport, times(1)).setDataSource(contains("AND cn.concept_name_type = 'SHORT'"), any());
    }

    @Test
    public void shouldDisplayFullySpecifiedWhenConceptNameDisplayFormatNotEqualsShortNamePreferredInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisCountReportConfig(false, false, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisCountReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);
        verify(mockJasperReport, times(1)).setDataSource(contains("AND cn.concept_name_type = 'FULLY_SPECIFIED'"), any());
    }

    private TSIntegrationDiagnosisCountReportConfig getMockTerminologyDiagnosisCountReportConfig(boolean displayTerminologyCodeFlag, boolean displayGenderFlag, boolean shortNamePreferredFlag) {
        TSIntegrationDiagnosisCountReportConfig tsIntegrationDiagnosisCountReportConfig = new TSIntegrationDiagnosisCountReportConfig();
        tsIntegrationDiagnosisCountReportConfig.setDisplayTerminologyCode(displayTerminologyCodeFlag);
        tsIntegrationDiagnosisCountReportConfig.setDisplayGenderGroup(displayGenderFlag);
        tsIntegrationDiagnosisCountReportConfig.setTerminologyParentCode("dummyCode");
        if (shortNamePreferredFlag)
            tsIntegrationDiagnosisCountReportConfig.setConceptNameDisplayFormat("shortNamePreferred");
        return tsIntegrationDiagnosisCountReportConfig;
    }

    private String getMockTerminologyDescendants() {
        return FileReaderUtil.getFileContent("terminologyServices/descendantCodes.json");
    }

}