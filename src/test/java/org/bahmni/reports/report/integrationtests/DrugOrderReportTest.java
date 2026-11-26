package org.bahmni.reports.report.integrationtests;

import org.bahmni.reports.builder.VisitBuilder;
import org.bahmni.reports.wrapper.CsvReport;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.context.Context;

import static org.junit.Assert.assertEquals;


public class DrugOrderReportTest extends BaseIntegrationTest {

    @Before
    public void setUp() throws Exception {

        Patient patient = Context.getPatientService().getPatient(2);

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
        CsvReport drugOrderReport = fetchCsvReport("Drug Order report","2016-02-01","2016-02-29");
        assertEquals(14, drugOrderReport.columnsCount());
        // One row per order. The report joins patient_identifier on preferred = 1 and does not exclude voided
        // identifiers. The voided identifier GAN123456 was marked preferred, which doubled every row (16).
        // It is now not preferred, so only the live identifier matches.
        assertEquals(8, drugOrderReport.rowsCount());
    }
}