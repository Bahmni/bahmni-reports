package org.bahmni.reports.report;

import org.bahmni.reports.report.integrationtests.BaseIntegrationTest;
import org.bahmni.reports.wrapper.CsvReport;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IpdOpdVisitCountReportTest extends BaseIntegrationTest {
    public IpdOpdVisitCountReportTest() {
        super("src/test/resources/config/ipdOpdVisitCountReportConfig.json");
    }

    @Before
    public void setUp() throws Exception {
        executeDataSet("datasets/ipdOpdVisitCountReportDataSet.xml");
    }

    @Test
    public void shouldFallBackToVisitTypeAndIgnoreVoidedAttributesAndVisits() throws Exception {
        CsvReport report = fetchCsvReport("IPD OPD Visit Count", "2016-04-01", "2016-04-30");

        assertEquals(1, report.rowsCount());
        // New OPD, Old OPD, Total OPD, New IPD, Old IPD, Total IPD
        assertEquals("0 1 1 0 2 2", report.getRowAsString(1, " "));
    }
}
