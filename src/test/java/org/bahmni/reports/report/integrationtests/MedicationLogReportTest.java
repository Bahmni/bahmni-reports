package org.bahmni.reports.report.integrationtests;


import com.google.gson.JsonObject;
import org.bahmni.reports.builder.*;
import org.bahmni.reports.wrapper.CsvReport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.context.Context;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@Ignore
public class MedicationLogReportTest extends BaseIntegrationTest {

    @Before
    public void setUp() throws Exception {

        Concept medicationLogConcept = new ConceptBuilder().withClassId(11).withDataTypeId(4).withName("Medication log Template").withShortName("Medication Log").withDescription("Medication template").build();
        Context.getConceptService().saveConcept(medicationLogConcept);

        Concept tbTreatmentStartConcept = new ConceptBuilder().withClassId(11).withDataTypeId(4).withName("TB Treatment Start").withShortName("TB treatment start").withDescription("TB start").build();
        Context.getConceptService().saveConcept(tbTreatmentStartConcept);

        Concept typeOfTreatmentRegimen = new ConceptBuilder().withClassId(11).withDataTypeId(2).withName("Medication log, Type of treatment regimen").withShortName("Type of regimen").withDescription("regimen").build();
        Context.getConceptService().saveConcept(typeOfTreatmentRegimen);

        Concept onlyFirstLineDrugsConcept = new ConceptBuilder().withClassId(11).withDataTypeId(4).withName("Only 1st line drugs").withShortName("").withDescription("1st line drugs").build();
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

        Provider provider = Context.getProviderService().getProvider(1);


        EncounterType encounterType = Context.getEncounterService().getEncounterType(7);
        Encounter encounter = new EncounterBuilder().withPatient(patient).withDatetime("2015-11-02")
                .withDateCreated("2016-03-04").withVisit(visit)
                .withEncounterType(encounterType).build();
        Context.getEncounterService().saveEncounter(encounter);

        EncounterRole encounterRole = Context.getEncounterService().getEncounterRole(1);
        EncounterProvider encounterProvider = new EncounterProviderBuilder().buildWithProvider(provider).buildWithEncounterRoleId(encounterRole).buildWithVoided(false).buildWithEncounter(encounter).build();
        Set<EncounterProvider> encounterProviderSet = new HashSet<>();
        encounterProviderSet.add(encounterProvider);

        Context.getEncounterService().getEncounter(encounter.getEncounterId()).setEncounterProviders(encounterProviderSet);
        Context.getEncounterService().saveEncounter(encounter);

        Obs typeOfRegimenObs = new ObsBuilder().withConcept(typeOfTreatmentRegimen).withEncounter(encounter).withDatetime(DateUtil.parseDate("2015-11-02")).withValue(onlyFirstLineDrugsConcept).withPerson(person).build();

        Obs tbTreatmentStartObs = new ObsBuilder().withConcept(tbTreatmentStartConcept).withEncounter(encounter).withDatetime(DateUtil.parseDate("2015-11-02")).withGroupMembers(typeOfRegimenObs).withPerson(person).build();

        Obs medicationLogTemplateObs = new ObsBuilder().withConcept(medicationLogConcept).withEncounter(encounter).withDatetime(DateUtil.parseDate("2015-11-02")).withGroupMembers(tbTreatmentStartObs).withPerson(person).build();
        Context.getObsService().saveObs(medicationLogTemplateObs, "");

        ConceptSource conceptSource= new ConceptReferenceSourceBuilder().withName("EndTB").withDescription("dictionary").withDateCreated(DateUtil.parseDate("2015-11-02")).withRetired(false).build();
        Context.getConceptService().saveConceptSource(conceptSource);

        ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTermBuilder().withConceptSource(conceptSource).withCode("1234").withName("referenceterm").withDateCreated(DateUtil.parseDate("2015-11-02")).build();
        Context.getConceptService().saveConceptReferenceTerm(conceptReferenceTerm);

        ConceptMap conceptMap = new ConceptReferenceMapBuilder().withConcept(onlyFirstLineDrugsConcept).withConceptReferenceTerm(conceptReferenceTerm).withDateCreated(DateUtil.parseDate("2015-11-02")).build();
        Set<ConceptMap> conceptMaps = new HashSet<ConceptMap>();
        conceptMaps.add(conceptMap);

        onlyFirstLineDrugsConcept.setConceptMappings(conceptMaps);
        Context.getConceptService().saveConcept(onlyFirstLineDrugsConcept);

        JsonObject object = new JsonObject();

        object.addProperty("name", "Type of regimen");
        object.addProperty("fullName", "Medication log, Type of treatment regimen");
        object.add("units", null);
        object.add("hiNormal", null);
        object.add("lowNormal", null);

        List<JsonObject> objectList = new ArrayList<JsonObject>();
        objectList.add(object);

        when(httpClient.get(URI.create(bahmniReportsProperties.getOpenmrsRootUrl() + "/reference-data/leafConcepts?conceptName=Medication+log+Template"))).thenReturn(objectList.toString());
    }

    @Test
    public void shouldRetrieveMedicationReport() throws Exception {
        CsvReport report = fetchCsvReport("Medication Log Data Export", "2015-10-13", "2016-10-21");
        assertEquals(8, report.columnsCount());
        assertNotNull(report.getRow(1));
    }

    @Test
    public void shouldRetrieveReferenceCodeIfItisPresent() throws Exception {
        CsvReport report = fetchCsvReport("Medication Log Data Export", "2015-10-13", "2016-10-21");
        assertEquals(8, report.columnsCount());
        assertEquals("1234",report.getColumnValueInRow(1,"Type of regimen"));
    }

}
