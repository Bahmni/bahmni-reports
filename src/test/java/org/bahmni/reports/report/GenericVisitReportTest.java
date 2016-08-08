package org.bahmni.reports.report;

import org.bahmni.reports.report.integrationtests.BaseIntegrationTest;
import org.bahmni.reports.wrapper.CsvReport;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GenericVisitReportTest extends BaseIntegrationTest {
    public GenericVisitReportTest() {
        super("src/test/resources/config/genericVisitReportConfig.json");
    }

    @Before
    public void setUp() throws Exception {
        executeDataSet("datasets/genericVisitReportDataSet.xml");
    }

    @Test
    public void shouldFetchBasicColumnsIfNoConfigSpecified() throws Exception {
        String reportName = "Generic Visit Report Without Config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchVisitIdAndPatientIdAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Data Analysis";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(13, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005  2 2", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredPatientAttributesAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Patient Attributes";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(17, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005   10th pass   General", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredVisitAttributesAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Visit Attributes";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(13, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005   Admitted", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredAddressFieldsAlongWithBasicColumnsWhenConfigured() throws Exception {
        String reportName = "Generic Visit Report With Patient Addresses";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(13, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005   Dindori", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldConsiderVisitStopDateInDateRangeIfSpecified() throws Exception {
        String reportName = "Generic Visit Report With Visit Stopped Date For Date Range";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Return TB Clinic Visit 20-Mar-2016 21-Apr-2016", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldConsiderVisitStartDateInDateRangeIfSpecified() throws Exception {
        String reportName = "Generic Visit Report With Visit Start Date For Date Range";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByVisitTypesIfSpecified() throws Exception {
        String reportName = "Generic Visit Report Filtered By Visit Types";

        CsvReport report = fetchCsvReport(reportName, "2016-03-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchAllConfiguredData() throws Exception {
        String reportName = "Generic Visit Report With Full Config";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-05-30");

        assertEquals(22, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005   General 10th pass   Dindori Ramgarh  Admitted 2 2", report.getRowAsString(1, " "));
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Return TB Clinic Visit 20-Mar-2016 21-Apr-2016    General 10th pass   Dindori Ramgarh   2 3", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldNotFetchLocationInfoIfLocationIsVoided() throws Exception {
        String reportName = "Generic Visit Report Filtered By Voided Location Tags";

        CsvReport report = fetchCsvReport(reportName, "2017-04-01", "2017-05-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 23 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2017 21-May-2017 01-Jan-2005 01-Jan-2005", report.getRowAsString(1, " "));
        assertEquals("GAN1234 Horatio Hornblower 23 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2017 21-May-2017", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldExcludeColumnsSpecifedInTheConfig() throws Exception {
        String reportName = "Generic Visit Report With Excluded Column";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-05-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldThrowAnExceptionIfAllColumnsAreExcluded() throws Exception {

        String reportName = "Generic Visit Report With Excluded All Column Ignore Case";

        CsvReport report = fetchCsvReport(reportName, "2016-01-01", "2016-01-30", true);

        assertEquals("<h2>Incorrect Configuration</h2><h3>You have excluded all columns.</h3>", report.getReportName());
    }

    @Test
    public void shouldShowConfiguredExtraIdentifiers() throws Exception {
        String reportName = "Generic Visit Report With Extra Identifiers";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(13, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005  Adhar1 Pan1", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldShowConfiguredOneExtraIdentifier() throws Exception {
        String reportName = "Generic Visit Report With One Extra Identifiers";

        CsvReport report = fetchCsvReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(12, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005  Pan1", report.getRowAsString(1, " "));
    }

}