package org.bahmni.reports.report;

import org.bahmni.reports.wrapper.Report;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderFulfillmentReportITest extends BaseIntegrationTest {

    @Test
    public void shouldRetrieveOrderFulfillmentReport() throws Exception {
        Report report = fetchReport("Order Fulfillment Report", "2010-02-03", "2015-10-12", "text/csv", "A3");
        assertEquals(9, report.getNumberOfColumns());
        assertEquals("Order Fulfillment Report", report.getReportName());
        assertEquals("Drug Order Glipizide 15-Jan-2014 GAN200025 Dhyan Chand  M No", report.getRowAsString(1, " "));
        assertEquals("Drug Order", report.getColumnValueInRow(1, "Order Type"));
        assertEquals("GAN200025", report.getColumnValueInRow(1, "Patient ID"));
        assertEquals("Drug Order Metformin 15-Jan-2014 GAN200025 Dhyan Chand  M No", report.getRowAsString(2, " "));
    }
}