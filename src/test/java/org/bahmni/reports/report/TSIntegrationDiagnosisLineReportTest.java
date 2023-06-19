package org.bahmni.reports.report;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisLineReportConfig;
import org.bahmni.reports.model.TSIntegrationDiagnosisReportConfig;
import org.bahmni.reports.template.TSIntegrationDiagnosisLineReportTemplate;
import org.bahmni.reports.template.TSIntegrationDiagnosisReportTemplate;
import org.bahmni.webclients.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
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
    private Properties mockTsProperties;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tsIntegrationDiagnosisLineReportTemplate.setDescendantsUrlTemplate("dummyUrlTemplate");
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
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisLineReportConfig(true));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
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
        when(mockReport.getConfig()).thenReturn(getMockTerminologyDiagnosisLineReportConfig(false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.subtotalsAtSummary(any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockTerminologyDescendants());

        tsIntegrationDiagnosisLineReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(6)).addColumn(any());
    }


    private TSIntegrationDiagnosisLineReportConfig getMockTerminologyDiagnosisLineReportConfig(boolean displayTerminologyCodeFlag) {
        TSIntegrationDiagnosisLineReportConfig tsIntegrationDiagnosisLineReportConfig = new TSIntegrationDiagnosisLineReportConfig();
        tsIntegrationDiagnosisLineReportConfig.setDisplayTerminologyCode(displayTerminologyCodeFlag);
        tsIntegrationDiagnosisLineReportConfig.setTerminologyParentCode("dummyCode");
        return tsIntegrationDiagnosisLineReportConfig;
    }

    private String getMockTerminologyDescendants() throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("ts/descendantCodes.json").toURI());
        return Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
    }


}