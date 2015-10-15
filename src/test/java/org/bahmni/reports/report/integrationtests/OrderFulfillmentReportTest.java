package org.bahmni.reports.report.integrationtests;

import org.bahmni.reports.builder.OrderBuilder;
import org.bahmni.reports.builder.PersonBuilder;
import org.bahmni.reports.builder.PersonNameBuilder;
import org.bahmni.reports.wrapper.Report;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.context.Context;

import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class OrderFulfillmentReportTest extends BaseIntegrationTest {

    @Before
    public void beforeOrderFulfillmentReportTest() throws SQLException {
        Context.authenticate("admin", "test");
        OrderType orderType = Context.getOrderService().getOrderType(17);
        Patient patient = Context.getPatientService().getPatient(2);
        Provider provider = Context.getProviderService().getProvider(1);
        Encounter encounter1 = Context.getEncounterService().getEncounter(6);
        Concept concept = Context.getConceptService().getConceptByName("ASPIRIN");
        Concept anotherConcept = Context.getConceptService().getConceptByName("COUGH SYRUP");
        Order order = new OrderBuilder()
                .withOrderType(orderType)
                .withConcept(concept).withEncounter(encounter1)
                .withOrderer(provider).withPatient(patient).withDateActivated("2015-10-16").build();
        Context.getOrderService().saveOrder(order, null);
        Order secondOrder = new OrderBuilder()
                .withOrderType(orderType)
                .withConcept(anotherConcept).withEncounter(encounter1)
                .withOrderer(provider).withPatient(patient).withDateActivated("2015-10-16").build();
        Context.getOrderService().saveOrder(secondOrder, null);
        getConnection().commit();
    }

    @Test
    public void shouldRetrieveOrderFulfillmentReport() throws Exception {
        Report report = fetchReport("Order Fulfillment Report", "2015-10-13", "2015-10-21", "text/csv", "A3");
        assertEquals(9, report.getNumberOfColumns());
        assertEquals("Order Fulfillment Report", report.getReportName());
        assertEquals("Plain Order ASPIRIN 16-Oct-2015 101 Horatio Hornblower  M No", report.getRowAsString(1, " "));
        assertEquals("Plain Order", report.getColumnValueInRow(1, "Order Type"));
        assertEquals("101", report.getColumnValueInRow(1, "Patient ID"));
        assertEquals("Plain Order COUGH SYRUP 16-Oct-2015 101 Horatio Hornblower  M No", report.getRowAsString(2, " "));
    }
}