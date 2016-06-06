package org.bahmni.reports.report;

import org.bahmni.reports.report.integrationtests.BaseIntegrationTest;
import org.bahmni.reports.wrapper.Report;
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

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchVisitIdAndPatientIdAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Data Analysis";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(13, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005  2 2", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredPatientAttributesAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Patient Attributes";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(17, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005   10th pass   General", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredVisitAttributesAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Visit Attributes";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(13, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005   Admitted", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredAddressFieldsAlongWithBasicColumnsWhenConfigured() throws Exception {
        String reportName = "Generic Visit Report With Patient Addresses";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(13, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005   Dindori", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldConsiderVisitStopDateInDateRangeIfSpecified() throws Exception {
        String reportName = "Generic Visit Report With Visit Stopped Date For Date Range";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Return TB Clinic Visit 20-Mar-2016 21-Apr-2016", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldConsiderVisitStartDateInDateRangeIfSpecified() throws Exception {
        String reportName = "Generic Visit Report With Visit Start Date For Date Range";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByVisitTypesIfSpecified() throws Exception {
        String reportName = "Generic Visit Report Filtered By Visit Types";

        Report report = fetchReport(reportName, "2016-03-01", "2016-04-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchAllConfiguredData() throws Exception {
        String reportName = "Generic Visit Report With Full Config";

        Report report = fetchReport(reportName, "2016-04-01", "2016-05-30");

        assertEquals(22, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 01-Jan-2005   General 10th pass   Dindori Ramgarh  Admitted 2 2", report.getRowAsString(1, " "));
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M 15-Aug-2008 Return TB Clinic Visit 20-Mar-2016 21-Apr-2016    General 10th pass   Dindori Ramgarh   2 3", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldNotFetchLocationInfoIfLocationIsVoided() throws Exception {
        String reportName = "Generic Visit Report Filtered By Voided Location Tags";

        Report report = fetchReport(reportName, "2017-04-01", "2017-05-30");

        assertEquals(11, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 23 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2017 21-May-2017 01-Jan-2005 01-Jan-2005", report.getRowAsString(1, " "));
        assertEquals("GAN1234 Horatio Hornblower 23 02-Oct-1993 M 15-Aug-2008 Initial HIV Clinic Visit 20-Apr-2017 21-May-2017", report.getRowAsString(2, " "));
    }
}