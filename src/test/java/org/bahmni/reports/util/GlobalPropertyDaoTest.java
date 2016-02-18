package org.bahmni.reports.util;

import org.bahmni.reports.report.integrationtests.BaseIntegrationTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GlobalPropertyDaoTest extends BaseIntegrationTest {
    @Test
    public void shouldFetchPasswordForReportsUser() throws Exception {
        String reportUserPassword = GlobalPropertyDao.getReportUserPassword(getDatabaseConnection());

        assertNotNull(reportUserPassword);
        assertEquals("password", reportUserPassword);
    }
}