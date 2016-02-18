package org.bahmni.reports.report.integrationtests;

import org.bahmni.reports.builder.VisitBuilder;
import org.bahmni.reports.wrapper.Report;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.context.Context;

import static org.junit.Assert.assertEquals;


public class DrugOrderReportTest extends BaseIntegrationTest {

    @Before
    public void setUp() throws Exception {

        Person person = Context.getPersonService().getPerson(2);

        Patient patient = Context.getPatientService().getPatient(2);
        PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByName("Bahmni Id");
        Location location = Context.getLocationService().getLocation(1);

        PatientIdentifier patientIdentifier = new PatientIdentifier("SEM123-0", patientIdentifierType, location);
        patientIdentifier.setPatient(patient);
        Context.getPatientService().savePatientIdentifier(patientIdentifier);

        Visit visit1 = new VisitBuilder().withPatient(patient).withVisitType(1).withStartDate("2016-01-17").build();
        Context.getVisitService().saveVisit(visit1);

        Visit visit2 = new VisitBuilder().withPatient(patient).withVisitType(1).withStartDate("2016-02-17").build();
        Context.getVisitService().saveVisit(visit2);


        executeDataSet("datasets/DrugOrderReportTest-DrugOrders.xml");
        getConnection().commit();
        Context.clearSession();

    }

    @Test
    public void shouldRetrieveDrugOrderReportWithFourteenColmns() throws Exception {
        Report drugOrderReport = fetchReport("Drug Order report","2016-02-01","2016-02-29");
        assertEquals(14, drugOrderReport.getNumberOfColumns());
    }

    @Test
    public void shouldRetrieveDrugOrderReportWithFourRecords() throws Exception {
        Report drugOrderReport = fetchReport("Drug Order report","2016-02-01","2016-02-29");
        assertEquals(4, drugOrderReport.numberOfRows());
    }
}