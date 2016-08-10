package org.bahmni.reports.report;

import org.bahmni.reports.report.integrationtests.BaseIntegrationTest;
import org.bahmni.reports.wrapper.CsvReport;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class GenericProgramReportTest extends BaseIntegrationTest {
    public GenericProgramReportTest() {
        super("src/test/resources/config/genericProgramReportConfig.json");
    }

    @Before
    public void setUp() throws Exception {
        executeDataSet("datasets/genericProgramReportDataSet.xml");
    }

    @Test
    public void shouldFetchBasicColumnsIfNoConfigSpecified() throws Exception {
        String reportName = "Generic Patient Program Report Without Config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(4, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2", report.getRowAsString(4, " "));
    }


    @Test
    public void shouldFetchBasicColumnsOnlyIfEmptyConfigSpecified() throws Exception {
        String reportName = "Generic Patient Program Report With Empty Config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(4, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2", report.getRowAsString(4, " "));
    }

    @Test
    public void shouldFetchProgramsEnrolledWithInSpecifiedDateRange() throws Exception {
        String reportName = "Generic Patient Program Report Without Config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-20", "2016-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(3, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(3, " "));
    }

    @Test
    public void shouldNotThrowErrorIfStartAndEndDatesAreEmpty() throws Exception {
        String reportName = "Generic Patient Program Report Without Config";

        CsvReport report = fetchCsvReport(reportName, "", "");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(0, report.rowsCount());
    }

    @Test
    public void shouldFetchConfiguredPatientAttributesAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Patient Program Report With Patient Attributes";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(15, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(4, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2 10th pass  8763245677 General", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1 10th pass  8763245677 General", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2 10th pass  8763245677 General", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2 10th pass  8763245677 General", report.getRowAsString(4, " "));
    }

    @Test
    public void shouldNotFetchPatientAttributesWhenEmptyListIsConfigured() throws Exception {
        String reportName = "Generic Patient Program Report With Empty Patient Attributes";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(4, report.rowsCount());

        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2", report.getRowAsString(4, " "));
    }

    @Test
    public void shouldNotFetchPersonAddressesWhenEmptyListIsConfigured() throws Exception {
        String reportName = "Generic Patient Program Report With Empty Patient Addresses";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(4, report.rowsCount());


        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2", report.getRowAsString(4, " "));
    }

    @Test
    public void shouldFetchConfiguredPatientAddressesAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Patient Program Report With Patient Addresses";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(12, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(4, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2 Ramgarh Dindori", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1 Ramgarh Dindori", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2 Hyderabad Address Three", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2 Hyderabad Address Three", report.getRowAsString(4, " "));
    }

    @Test
    public void shouldFetchActiveProgramsWithInTheDateRange() throws Exception {
        String reportName = "Generic Patient Program Report Without Config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-31", "2016-05-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(10, report.columnsCount());
        assertEquals(1, report.rowsCount());
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchFieldsForDataAnalysisAlongWithBasic() throws Exception {
        String reportName = "Generic Patient Program Report For Data Analysis";

        CsvReport report = fetchCsvReport(reportName, "2016-04-31", "2016-05-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(13, report.columnsCount());
        assertEquals(1, report.rowsCount());
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2 1,001 1", report.getRowAsString(1, " "));

    }

    @Test
    public void shouldFetchBasicFieldsWithDataAnalysisFalse() throws Exception {
        String reportName = "Generic Patient Program Report For Data Analysis False";

        CsvReport report = fetchCsvReport(reportName, "2016-04-31", "2016-05-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(10, report.columnsCount());
        assertEquals(1, report.rowsCount());
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredProgramAttributes() throws Exception {
        String reportName = "Generic Patient Program Report With Program Attributes";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(13, report.columnsCount());
        assertEquals(4, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2   true", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1  FOLLOWING false", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2  DIED true", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2", report.getRowAsString(4, " "));
    }

    @Test
    public void shouldFetchDataFilteredByConfiguredProgramName() throws Exception {
        String reportName = "Generic Patient Program Report Filtered By Program Names";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(10, report.columnsCount());
        assertEquals(2, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1", report.getRowAsString(1, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldFetchBasicColumnsWhenProgramNamesAreEmpty() throws Exception {
        String reportName = "Generic Patient Program Report With Empty Program Names";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(10, report.columnsCount());
        assertEquals(4, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2", report.getRowAsString(4, " "));
    }

    @Test
    public void shouldFetchAllStatesWhenConfiguredToShowAll() throws Exception {
        String reportName = "Generic Patient Program Report With All The States";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(12, report.columnsCount());
        assertEquals(6, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 1 19-Apr-2016 22-Apr-2016", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2 21-Apr-2016", report.getRowAsString(2, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1 21-Apr-2016", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 1 19-Apr-2016 20-Apr-2016", report.getRowAsString(4, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2 21-Apr-2016", report.getRowAsString(5, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2 21-Apr-2016", report.getRowAsString(6, " "));
    }

    @Test
    public void shouldFetchAllTheDataWithFullConfig() throws Exception {
        String reportName = "Generic Patient Program Report With Full Config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(25, report.columnsCount());
        assertEquals(2, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1 21-Apr-2016  10th pass  8763245677 General   FOLLOWING false Ramgarh Dindori 1,000 2", report.getRowAsString(1, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2 21-Apr-2016  10th pass  8763245677 General     Hyderabad Address Three 1,001 2", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldFetchAllTheStatesForAParticularPatientWhenConfiguredToSeeAll() throws Exception {
        String reportName = "Generic Patient Program To See All States As Multiple Rows";

        CsvReport report = fetchCsvReport(reportName, "2016-04-19", "2016-04-23");

        assertEquals(reportName, report.getReportName());
        assertEquals(12, report.columnsCount());
        assertEquals(6, report.rowsCount());

        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 1 19-Apr-2016 22-Apr-2016", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2 21-Apr-2016", report.getRowAsString(2, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1 21-Apr-2016", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 1 19-Apr-2016 20-Apr-2016", report.getRowAsString(4, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2 21-Apr-2016", report.getRowAsString(5, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2 21-Apr-2016", report.getRowAsString(6, " "));
    }

    @Test
    public void shouldFetchTheProgramEvenThoughIfItDoesNotHaveState() throws Exception{
        String reportName = "Generic Patient Program Report Without Config";

        CsvReport report = fetchCsvReport(reportName, "2017-04-01", "2017-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2", report.getRowAsString(1, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2017 21-Apr-2017", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldExcludeColumnsSpecifedInTheConfig() throws Exception {
        String reportName = "Generic Patient Program Report With Excluded Column";


        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(reportName, report.getReportName());
        assertEquals(23, report.columnsCount());
        assertEquals(2, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1 21-Apr-2016  8763245677 General   FOLLOWING false Ramgarh Dindori 1,000 2", report.getRowAsString(1, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2 21-Apr-2016  8763245677 General     Hyderabad Address Three 1,001 2", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldThrowAnExceptionIfAllColumnsAreExcluded() throws Exception {

        String reportName = "Generic Patient Program Report With Excluded All Column Ignore case";

        CsvReport report = fetchCsvReport(reportName, "2016-01-01", "2016-01-30", true);

        assertEquals("<h2>Incorrect Configuration</h2><h3>You have excluded all columns.</h3>", report.getReportName());
    }

    @Test
    public void shouldFetchReportWithExtraIdentifiers() throws Exception {
        String reportName = "Generic Patient Program Report with extra identifiers";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(12, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(4, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2 Aadhar1 Pan1", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1 Aadhar1 Pan1", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2 Aadhar2 Pan2", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2 Aadhar2 Pan2", report.getRowAsString(4, " "));
    }

    @Test
    public void shouldFetchReportWithOneExtraIdentifiers() throws Exception {
        String reportName = "Generic Patient Program Report with one extra identifiers";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(4, report.rowsCount());
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 HIV PROGRAM 20-Apr-2016 30-Apr-2016 State 2 Pan1", report.getRowAsString(1, " "));
        assertEquals("prog1 Generic Program1 7 15-Aug-2008 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 30-Apr-2016 State 1 Pan1", report.getRowAsString(2, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 HIV PROGRAM 19-Apr-2016  State 2 Pan2", report.getRowAsString(3, " "));
        assertEquals("prog2 Generic Program2 22 15-Aug-1994 F 15-Aug-2008 MDR-TB PROGRAM 19-Apr-2016 19-Apr-2016 State 2 Pan2", report.getRowAsString(4, " "));
    }

}