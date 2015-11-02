package org.bahmni.reports.report.integrationtests;

import org.bahmni.reports.builder.ConceptBuilder;
import org.bahmni.reports.builder.DateUtil;
import org.bahmni.reports.builder.EncounterBuilder;
import org.bahmni.reports.builder.OrderBuilder;
import org.bahmni.reports.builder.VisitBuilder;
import org.bahmni.reports.wrapper.Report;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class OrderFulfillmentReportTest extends BaseIntegrationTest {

    @Before
    public void setUp() {
        Concept concept = new ConceptBuilder().withClassId(3).withDataTypeId(2).withName("Plain Concept").withShortName("Plain Concept").build();
        Context.getConceptService().saveConcept(concept);

        Concept anotherConcept = new ConceptBuilder().withClassId(3).withDataTypeId(2).withName("Plain concept 2").withShortName("Plain concpet 2").build();
        Context.getConceptService().saveConcept(anotherConcept);

        Patient patient = Context.getPatientService().getPatient(2);
        PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByName("Bahmni Id");
        Location location = Context.getLocationService().getLocation(1);

        PatientIdentifier patientIdentifier = new PatientIdentifier("SEM123-0", patientIdentifierType, location);
        patientIdentifier.setPatient(patient);
        Context.getPatientService().savePatientIdentifier(patientIdentifier);


        Visit visit = new VisitBuilder().withPatient(patient).withVisitType(1).withStartDate("2015-11-02").build();
        Context.getVisitService().saveVisit(visit);

        EncounterType encounterType = Context.getEncounterService().getEncounterType(1);
        Encounter encounter = new EncounterBuilder().withPatient(patient).withDatetime("2015-11-02").withVisit(visit).withEncounterType(encounterType).build();
        Context.getEncounterService().saveEncounter(encounter);

        Provider provider = Context.getProviderService().getProvider(1);

        OrderType orderType = Context.getOrderService().getOrderType(17);
        Order order = new OrderBuilder().withOrderType(orderType).setDateActivated("2015-11-02").withConcept(concept).withPatient(patient).withEncounter(encounter).withOrderer(provider).build();
        Context.getOrderService().saveOrder(order, null);
    }

    @Test
    public void shouldRetrieveOrderFulfillmentReport() throws Exception {
        Report report = fetchReport("Order Fulfillment Report", "2000-10-13", "2016-10-21");
        assertEquals(9, report.getNumberOfColumns());
        assertEquals("Order Fulfillment Report", report.getReportName());
        assertEquals("Plain Order Plain Concept 02-Nov-2015 SEM123-0 Horatio Hornblower  M No", report.getRowAsString(1, " "));
        assertEquals("Plain Order", report.getColumnValueInRow(1, "Order Type"));
        assertEquals("SEM123-0", report.getColumnValueInRow(1, "Patient ID"));
    }
}