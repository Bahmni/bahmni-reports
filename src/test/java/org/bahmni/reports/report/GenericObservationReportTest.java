package org.bahmni.reports.report;

import org.bahmni.reports.report.integrationtests.BaseIntegrationTest;
import org.bahmni.reports.wrapper.CsvReport;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenericObservationReportTest extends BaseIntegrationTest {
    public GenericObservationReportTest() {
        super("src/test/resources/config/genericObservationReportConfig.json");
    }

    @Before
    public void setUp() throws Exception {
        executeDataSet("datasets/genericObservationReportDataSet.xml");
    }

    @Test
    public void shouldFetchMandatoryColumnsIfNoConfigSpecified() throws Exception {
        String reportName = "Observation report without any config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 170 2016-04-21 15:30:31.0", report.getRowAsString(1, " "));
    }


    @Test
    public void shouldFetchMandatoryColumnsIfEmptyConfigSpecified() throws Exception {
        String reportName = "Observation report with empty config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 170 2016-04-21 15:30:31.0", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchOneConceptPerRow() throws Exception {
        String reportName = "Observation report without any config";

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Vitals  2016-06-01 10:20:00.0", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 weight 80 2016-06-01 10:20:00.0 Vitals", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldShowProviderInfoIfConfigured() throws Exception {
        String reportName = "Observation report with provider info";

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(15, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Vitals  2016-06-01 10:20:00.0  Clinical Provider", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals Clinical Provider", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 weight 80 2016-06-01 10:20:00.0 Vitals Clinical Provider", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldShowVisitInfoIfConfigured() throws Exception {
        String reportName = "Observation report with visit info";

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(17, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Vitals  2016-06-01 10:20:00.0  Initial HIV Clinic Visit 2016-06-01 00:00:00.0 2016-06-30 00:00:00.0", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals Initial HIV Clinic Visit 2016-06-01 00:00:00.0 2016-06-30 00:00:00.0", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 weight 80 2016-06-01 10:20:00.0 Vitals Initial HIV Clinic Visit 2016-06-01 00:00:00.0 2016-06-30 00:00:00.0", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldShowPatientAttributesIfConfigured() throws Exception {
        String reportName = "Observation report with patient attributes";

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(20, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Vitals  2016-06-01 10:20:00.0   10th pass  8763245677 General", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals  10th pass  8763245677 General", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 weight 80 2016-06-01 10:20:00.0 Vitals  10th pass  8763245677 General", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldShowVisitAttributesIfConfigured() throws Exception {
        String reportName = "Observation report with visit attributes";

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(16, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Vitals  2016-06-01 10:20:00.0  OPD", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals OPD", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 weight 80 2016-06-01 10:20:00.0 Vitals OPD", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldShowPatientAddressIfConfigured() throws Exception {
        String reportName = "Observation report with patient address";

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(16, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Vitals  2016-06-01 10:20:00.0   Dindori", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals  Dindori", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 weight 80 2016-06-01 10:20:00.0 Vitals  Dindori", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldShowDataAnalysisColumnsIfConfigured() throws Exception {
        String reportName = "Observation report with data analysis columns";

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(23, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Vitals  2016-06-01 10:20:00.0  1000 1100 1100 1100   1002 Vitals", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals 1000 1100 1100 1101 1100  1000 Height", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 weight 80 2016-06-01 10:20:00.0 Vitals 1000 1100 1100 1102 1100  1001 Weight weight", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldFilterByConceptNames() throws Exception {
        String reportName = "Observation report filtered by concept names";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Height\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Height"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByConceptClass() throws Exception {
        String reportName = "Observation report filtered by concept class";

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 180 2016-06-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 weight 80 2016-06-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldFilterByConceptNamesAndClass() throws Exception {
        String reportName = "Observation report filtered by concept names and class";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Vitals\"");
        objectList.add("\"Height\"");
        objectList.add("\"Weight\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Vitals"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-06-01", "2016-06-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Vitals  2016-06-01 10:20:00.0", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByLocationTags() throws Exception {
        String reportName = "Observation report filtered by location tags";

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Vitals  2016-08-01 10:20:00.0", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldFilterByConceptNamesAndLocationTags() throws Exception {
        String reportName = "Observation report filtered by concept names and location tags";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Height\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Height"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByConceptClassesAndLocationTags() throws Exception {
        String reportName = "Observation report filtered by concept class and location tags";

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldFilterByConceptNamesAndConceptClassesAndLocationTags() throws Exception {
        String reportName = "Observation report filtered by concept names and concept class and location tags";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Height\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Height"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByPrograms() throws Exception {
        String reportName = "Observation report filtered by program name";

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Vitals  2016-08-01 10:20:00.0", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldFilterByProgramsAndConceptNames() throws Exception {
        String reportName = "Observation report filtered by program name and concept name";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Height\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Height"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByProgramsAndLocationTags() throws Exception {
        String reportName = "Observation report filtered by program name and location tags";

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Vitals  2016-08-01 10:20:00.0", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldFilterByProgramsAndConceptNameAndLocationTags() throws Exception {
        String reportName = "Observation report filtered by program name and concept names and location tags";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Height\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Height"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByProgramsAndConceptClassFilter() throws Exception {
        String reportName = "Observation report filtered by program name and concept class";

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldFilterByProgramsAndConceptNameAndConceptClass() throws Exception {
        String reportName = "Observation report filtered by program name and concept names and concept class";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Vitals\"");
        objectList.add("\"Height\"");
        objectList.add("\"Weight\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Vitals"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldFilterByProgramsAndConceptNameAndConceptClassAndLocationTags() throws Exception {
        String reportName = "Observation report filtered by program name and concept names and concept class and location tags";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Vitals\"");
        objectList.add("\"Height\"");
        objectList.add("\"Weight\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Vitals"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldFilterByVisitTypes() throws Exception {
        String reportName = "Observation report filtered by visit types";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Vitals\"");
        objectList.add("\"Height\"");
        objectList.add("\"Weight\"");

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Vitals"))).thenReturn(objectList.toString());

        CsvReport report = fetchCsvReport(reportName, "2016-04-21", "2016-05-23");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari HIV PROGRAM 20-Apr-2016 30-Jun-2016 15-Aug-2008 Height 170 2016-04-21 15:30:31.0", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldApplyDateRangeFilterForVisitStartDate() throws Exception {
        String reportName = "Observation report apply date range for visit start date";

        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-01");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(6, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Vitals  2016-08-01 10:20:00.0", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(3, " "));
        assertEquals("OBS1 Generic Observation1   F Chithari    15-Aug-2008 Vitals  2016-08-02 00:00:00.0", report.getRowAsString(4, " "));
        assertEquals("OBS1 Generic Observation1   F Chithari    15-Aug-2008 Height 170 2016-08-02 00:00:00.0 Vitals", report.getRowAsString(5, " "));
        assertEquals("OBS1 Generic Observation1   F Chithari    15-Aug-2008 weight 70 2016-08-02 00:00:00.0 Vitals", report.getRowAsString(6, " "));
    }

    @Test
    public void shouldApplyDateRangeFilterForVisitStopDate() throws Exception {
        String reportName = "Observation report apply date range for visit stop date";

        CsvReport report = fetchCsvReport(reportName, "2016-08-30", "2016-08-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(6, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Vitals  2016-08-01 10:20:00.0", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 Height 180 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(2, " "));
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 weight 80 2016-08-01 10:20:00.0 Vitals", report.getRowAsString(3, " "));
        assertEquals("OBS1 Generic Observation1   F Chithari    15-Aug-2008 Vitals  2016-08-02 00:00:00.0", report.getRowAsString(4, " "));
        assertEquals("OBS1 Generic Observation1   F Chithari    15-Aug-2008 Height 170 2016-08-02 00:00:00.0 Vitals", report.getRowAsString(5, " "));
        assertEquals("OBS1 Generic Observation1   F Chithari    15-Aug-2008 weight 70 2016-08-02 00:00:00.0 Vitals", report.getRowAsString(6, " "));
    }

    @Test
    public void shouldFetchAllDataInOneRowPerEncounter() throws Exception {
        String reportName = "Observation report encounter per row";

        List<String> objectList = new ArrayList<>();
        objectList.add("\"Height\"");
        objectList.add("\"Weight\"");

        URI getLeafConceptsUri = URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/leafConceptNames?conceptNames=Vitals");
        URI getChildConceptsUri = URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/getChildConcepts?conceptNames=Vitals");
        when(httpClient.get(getLeafConceptsUri)).thenReturn(objectList.toString());


        CsvReport report = fetchCsvReport(reportName, "2016-08-01", "2016-08-30");

        verify(httpClient, never()).get(getChildConceptsUri);
        assertEquals(12, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("OBS1 Generic Observation1   F Ganiyari MDR-TB PROGRAM 01-Aug-2016  15-Aug-2008 180 80", report.getRowAsString(1, " "));
        assertEquals("OBS1 Generic Observation1   F Chithari    15-Aug-2008 170 70", report.getRowAsString(2, " "));
    }
}