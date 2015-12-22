package org.bahmni.reports.report.integrationtests;


import com.google.gson.JsonObject;
import org.bahmni.reports.builder.ConceptBuilder;
import org.bahmni.reports.builder.EncounterBuilder;
import org.bahmni.reports.builder.ObsBuilder;
import org.bahmni.reports.builder.VisitBuilder;
import org.bahmni.reports.wrapper.Report;
import org.bahmni.webclients.Authenticator;
import org.bahmni.webclients.ConnectionDetails;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.context.Context;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;



public class MedicationLogReportTest extends BaseIntegrationTest {

    private static  ConnectionDetails connectionDetails;
    private static Authenticator authenticator;

    @Before
    public void setUp() throws Exception {

        Concept medicationLogConcept = new ConceptBuilder().withClassId(11).withDataTypeId(4).withName("Medication log Template").withShortName("Medication Log").build();
        Context.getConceptService().saveConcept(medicationLogConcept);

        Concept tbTreatmentStartConcept = new ConceptBuilder().withClassId(11).withDataTypeId(4).withName("TB Treatment Start").withShortName("TB treatment start").build();
        Context.getConceptService().saveConcept(tbTreatmentStartConcept);

        Concept typeOfTreatmentRegimen = new ConceptBuilder().withClassId(11).withDataTypeId(2).withName("Medication log, Type of treatment regimen").withShortName("Type of regimen").build();
        Context.getConceptService().saveConcept(typeOfTreatmentRegimen);

        Concept onlyFirstLineDrugsConcept = new ConceptBuilder().withClassId(11).withDataTypeId(4).withName("Only 1st line drugs").withShortName("").build();
        Context.getConceptService().saveConcept(onlyFirstLineDrugsConcept);


        Person person = Context.getPersonService().getPerson(2);

        Patient patient = Context.getPatientService().getPatient(2);
        PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByName("Bahmni Id");
        Location location = Context.getLocationService().getLocation(1);

        PatientIdentifier patientIdentifier = new PatientIdentifier("SEM123-0", patientIdentifierType, location);
        patientIdentifier.setPatient(patient);
        Context.getPatientService().savePatientIdentifier(patientIdentifier);


        Visit visit = new VisitBuilder().withPatient(patient).withVisitType(1).withStartDate("2015-11-02").build();
        Context.getVisitService().saveVisit(visit);

        EncounterType encounterType = Context.getEncounterService().getEncounterType(7);
        Encounter encounter = new EncounterBuilder().withPatient(patient).withDatetime("2015-11-02").withVisit(visit).withEncounterType(encounterType).build();
        Context.getEncounterService().saveEncounter(encounter);

        Provider provider = Context.getProviderService().getProvider(1);

        Obs typeOfRegimenObs = new ObsBuilder().withConcept(typeOfTreatmentRegimen).withEncounter(encounter).withDatetime(Date.from(Instant.now())).withValue(onlyFirstLineDrugsConcept).withPerson(person).build();
        Context.getObsService().saveObs(typeOfRegimenObs, "");

        Obs tbTreatmentStartObs = new ObsBuilder().withConcept(tbTreatmentStartConcept).withEncounter(encounter).withDatetime(Date.from(Instant.now())).withGroupMembers(typeOfRegimenObs).withPerson(person).withPreviousVersion(typeOfRegimenObs).build();
        Context.getObsService().saveObs(tbTreatmentStartObs, "");

        Obs medicationLogTemplateObs = new ObsBuilder().withConcept(medicationLogConcept).withEncounter(encounter).withDatetime(Date.from(Instant.now())).withGroupMembers(tbTreatmentStartObs).withPerson(person).withPreviousVersion(tbTreatmentStartObs).build();
        Context.getObsService().saveObs(medicationLogTemplateObs, "");

        JsonObject object = new JsonObject();

        object.addProperty("name", "Type of regimen");
        object.addProperty("fullName", "Medication log, Type of treatment regimen");
        object.add("units", null);
        object.add("hiNormal", null);
        object.add("lowNormal", null);

        List<JsonObject> objectList = new ArrayList<JsonObject>();
        objectList.add(object);


//        String a = "[{'name':'Type of regimen','fullName':'Medication log, Type of treatment regimen','units':null,'hiNormal':null,'lowNormal':null}]";
        when(httpClient.get(URI.create("http://192.168.33.10:8080/openmrs/ws/rest/v1/reference-data/leafConcepts?conceptName=Medication+log+Template"))).thenReturn(objectList.toString());
    }



    @Test
    public void shouldRetrieveMedicationReport() throws Exception {
        Report report = fetchReport("Medication Log Data Export", "2015-10-13", "2016-10-21");
        assertEquals(9, report.getNumberOfColumns());
    }
}
