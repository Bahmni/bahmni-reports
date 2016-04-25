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

        assertEquals(8, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2016 21-May-2016", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchVisitIdAndPatientIdAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Data Analysis";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2016 21-May-2016 2 2", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredPatientAttributesAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Patient Attributes";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(14, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2016 21-May-2016  10th pass   General", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredVisitAttributesAlongWithBasicColumns() throws Exception {
        String reportName = "Generic Visit Report With Visit Attributes";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2016 21-May-2016  Admitted", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchConfiguredAddressFieldsAlongWithBasicColumnsWhenConfigured() throws Exception {
        String reportName = "Generic Visit Report With Patient Addresses";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(10, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2016 21-May-2016  Dindori", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldConsiderVisitStopDateInDateRangeIfSpecified() throws Exception {
        String reportName = "Generic Visit Report With Visit Stopped Date For Date Range";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(8, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Return TB Clinic Visit 20-Mar-2016 21-Apr-2016", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldConsiderVisitStartDateInDateRangeIfSpecified() throws Exception {
        String reportName = "Generic Visit Report With Visit Start Date For Date Range";

        Report report = fetchReport(reportName, "2016-04-01", "2016-04-30");

        assertEquals(8, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2016 21-May-2016", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFilterByVisitTypesIfSpecified() throws Exception {
        String reportName = "Generic Visit Report Filtered By Visit Types";

        Report report = fetchReport(reportName, "2016-03-01", "2016-04-30");

        assertEquals(8, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(1, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2016 21-May-2016", report.getRowAsString(1, " "));
    }

    @Test
    public void shouldFetchAllConfiguredData() throws Exception {
        String reportName = "Generic Visit Report With Full Config";

        Report report = fetchReport(reportName, "2016-04-01", "2016-05-30");

        assertEquals(19, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2016 21-May-2016  General 10th pass   Dindori Ramgarh  Admitted 2 2", report.getRowAsString(1, " "));
        assertEquals("GAN1234 Horatio Hornblower 22 02-Oct-1993 M Return TB Clinic Visit 20-Mar-2016 21-Apr-2016  General 10th pass   Dindori Ramgarh   2 3", report.getRowAsString(2, " "));
    }

    @Test
    public void shouldNotFetchLocationInfoIfLocationIsVoided() throws Exception {
        String reportName = "Generic Visit Report Filtered By Voided Location Tags";

        Report report = fetchReport(reportName, "2017-04-01", "2017-05-30");

        assertEquals(8, report.columnsCount());
        assertEquals(reportName, report.getReportName());
        assertEquals(2, report.rowsCount());
        assertEquals("GAN1234 Horatio Hornblower 23 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2017 21-May-2017", report.getRowAsString(1, " "));
        assertEquals("GAN1234 Horatio Hornblower 23 02-Oct-1993 M Initial HIV Clinic Visit 20-Apr-2017 21-May-2017", report.getRowAsString(1, " "));
    }
}