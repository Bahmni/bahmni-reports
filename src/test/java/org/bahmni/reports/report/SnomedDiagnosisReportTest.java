package org.bahmni.reports.report;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SnomedDiagnosisReportConfig;
import org.bahmni.reports.template.SnomedDiagnosisReportTemplate;
import org.bahmni.webclients.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
public class SnomedDiagnosisReportTest {
    @InjectMocks
    SnomedDiagnosisReportTemplate snomedDiagnosisReportTemplate;
    @Mock
    Report<SnomedDiagnosisReportConfig> mockReport;
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

    @Before
    public void setUp() {
    }

    @Test
    public void shouldUseSnomedDiagnosisReportTemplateWhenSnomedDiagnosisReportTypeIsInvoked() {
        SnomedDiagnosisReportConfig snomedDiagnosisReportConfig = new SnomedDiagnosisReportConfig();
        SnomedDiagnosisReport snomedDiagnosisReport = new SnomedDiagnosisReport();
        snomedDiagnosisReport.setConfig(snomedDiagnosisReportConfig);
        assertTrue(snomedDiagnosisReport.getTemplate(new BahmniReportsProperties()).getClass().isAssignableFrom(SnomedDiagnosisReportTemplate.class));
    }

    @Test
    public void shouldProcessFilteredDescendantsWithPagination() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockSnomedDiagnosisReportConfig(true, true));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockSnomedDescendants());


        snomedDiagnosisReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        //2 SNOMED Descendant Codes
        verify(mockPreparedStatement, times(2)).setString(eq(1), anyString());
        verify(mockPreparedStatement, times(2)).addBatch();
        //Single Pagination
        verify(mockPreparedStatement, times(1)).executeBatch();
    }

    @Test
    public void shouldIncludeBothSnomedCodeAndGenderGroupColumnsInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockSnomedDiagnosisReportConfig(true, true));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockSnomedDescendants());

        snomedDiagnosisReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(7)).addColumn(any());
    }

    @Test
    public void shouldIncludeSnomedCodeAndExcludeGenderGroupColumnsInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockSnomedDiagnosisReportConfig(true, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockSnomedDescendants());

        snomedDiagnosisReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(3)).addColumn(any());
    }

    @Test
    public void shouldExcludeSnomedCodeAndIncludeGenderGroupColumnsInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockSnomedDiagnosisReportConfig(false, true));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockSnomedDescendants());

        snomedDiagnosisReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(6)).addColumn(any());
    }

    @Test
    public void shouldExcludeBothSnomedCodeAndGenderGroupColumnsInJasperReport() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        when(mockReport.getName()).thenReturn("dummyReport");
        when(mockReport.getConfig()).thenReturn(getMockSnomedDiagnosisReportConfig(false, false));

        when(mockJasperReport.setPageFormat(any(), any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setReportName(anyString())).thenReturn(mockJasperReport);
        when(mockJasperReport.setTemplate(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setShowColumnTitle(anyBoolean())).thenReturn(mockJasperReport);
        when(mockJasperReport.setWhenNoDataType(any())).thenReturn(mockJasperReport);
        when(mockJasperReport.setDataSource(anyString(), any())).thenReturn(mockJasperReport);

        when(mockHttpClient.get(any(URI.class))).thenReturn(getMockSnomedDescendants());

        snomedDiagnosisReportTemplate.build(mockConnection, mockJasperReport, mockReport, "dummyStartDate", "dummyEndDate", null, PageType.A4);

        verify(mockJasperReport, times(2)).addColumn(any());
    }

    private SnomedDiagnosisReportConfig getMockSnomedDiagnosisReportConfig(boolean displaySnomedCodeFlag, boolean displayGenderFlag) {
        SnomedDiagnosisReportConfig snomedDiagnosisReportConfig = new SnomedDiagnosisReportConfig();
        snomedDiagnosisReportConfig.setDisplaySnomedCode(displaySnomedCodeFlag);
        snomedDiagnosisReportConfig.setDisplayGenderGroup(displayGenderFlag);
        snomedDiagnosisReportConfig.setSnomedParentCode("dummyCode");
        return snomedDiagnosisReportConfig;
    }

    private String getMockSnomedDescendants() throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("terminology/snomedDescendantCodes.json").toURI());
        return Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
    }

}