package org.bahmni.reports.web.security;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Reports;
import org.bahmni.webclients.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportAuthorizationTest {

    private static final String SESSION_ID = "sessionId";
    private ReportAuthorization reportAuthorization;

    @Mock
    private OpenMRSAuthenticator openMRSAuthenticator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BahmniReportsProperties bahmniReportsProperties;

    @Mock
    private HttpClient httpClient;

    // Use MockedStatic instead of PowerMock for static method mocking
    private MockedStatic<Reports> reportsMock;

    @Before
    public void setUp() {
        // Setup cookie for all tests
        String cookieName = "reporting_session";
        Cookie cookie = new Cookie(cookieName, SESSION_ID);
        Cookie[] cookies = new Cookie[]{cookie};
        when(request.getCookies()).thenReturn(cookies);

        // Initialize static mocking for Reports class
        reportsMock = Mockito.mockStatic(Reports.class);
    }

    @After
    public void tearDown() {
        // Important to close the static mock to avoid memory leaks
        if (reportsMock != null) {
            reportsMock.close();
        }
    }

    @Test
    public void shouldInvokeCallOpenMRSWithGivenSessionId() {
        // Setup
        Privileges privileges = mock(Privileges.class);
        when(openMRSAuthenticator.callOpenMRS(SESSION_ID)).thenReturn(ResponseEntity.ok(privileges));

        // Execute
        new ReportAuthorization(request, openMRSAuthenticator, bahmniReportsProperties, httpClient);

        // Verify
        verify(openMRSAuthenticator).callOpenMRS(SESSION_ID);
    }

    @Test
    public void shouldSetGivenPrivilegesAfterObjectCreation() throws IllegalAccessException {
        // Setup
        Privileges privileges = new Privileges();
        Privilege privilege = new Privilege();
        String privilegeName = "privilege";
        FieldUtils.writeField(privilege, "name", privilegeName, true);
        privileges.add(privilege);
        when(openMRSAuthenticator.callOpenMRS(SESSION_ID)).thenReturn(ResponseEntity.ok(privileges));

        // Execute
        reportAuthorization = new ReportAuthorization(request, openMRSAuthenticator, bahmniReportsProperties, httpClient);

        // Verify
        List userPrivileges = (List) FieldUtils.getDeclaredField(reportAuthorization.getClass(),
                "userPrivileges", true)
                .get(reportAuthorization);
        assertEquals(Collections.singletonList(privilegeName), userPrivileges);
    }

    @Test
    public void shouldReturnTrueIfUserHaveTheGivenReportPrivilege() throws Exception {
        // Setup
        Privileges privileges = mock(Privileges.class);
        when(openMRSAuthenticator.callOpenMRS(SESSION_ID)).thenReturn(ResponseEntity.ok(privileges));
        reportAuthorization = new ReportAuthorization(request, openMRSAuthenticator, bahmniReportsProperties, httpClient);
        String privilegeName = "privilege";
        FieldUtils.writeField(reportAuthorization, "userPrivileges",
                Collections.singletonList(privilegeName), true);

        Report report = mock(Report.class);
        when(report.getRequiredPrivilege()).thenReturn("privilege");

        // Mock static Reports.find method
        reportsMock.when(() -> Reports.find(eq("reportName"), any(), any())).thenReturn(report);

        // Execute
        boolean hasPrivilege = reportAuthorization.hasPrivilege("reportName");

        // Verify
        assertTrue(hasPrivilege);
        reportsMock.verify(() -> Reports.find(eq("reportName"), any(), any()));
    }

    @Test
    public void shouldReturnFalseIfUserHaveTheGivenReportPrivilege() throws Exception {
        // Setup
        Privileges privileges = mock(Privileges.class);
        when(openMRSAuthenticator.callOpenMRS(SESSION_ID)).thenReturn(ResponseEntity.ok(privileges));
        reportAuthorization = new ReportAuthorization(request, openMRSAuthenticator, bahmniReportsProperties, httpClient);
        FieldUtils.writeField(reportAuthorization, "userPrivileges",
                Collections.singletonList("userPrivilege"), true);

        Report report = mock(Report.class);
        when(report.getRequiredPrivilege()).thenReturn("otherPrivilege");

        // Mock static Reports.find method
        reportsMock.when(() -> Reports.find(eq("reportName"), any(), any())).thenReturn(report);

        // Execute
        boolean hasPrivilege = reportAuthorization.hasPrivilege("reportName");

        // Verify
        assertFalse(hasPrivilege);
        reportsMock.verify(() -> Reports.find(eq("reportName"), any(), any()));
    }

    @Test
    public void shouldReturnTrueIfReportHasNoPrivilege() throws Exception {
        // Setup
        Privileges privileges = mock(Privileges.class);
        when(openMRSAuthenticator.callOpenMRS(SESSION_ID)).thenReturn(ResponseEntity.ok(privileges));
        reportAuthorization = new ReportAuthorization(request, openMRSAuthenticator, bahmniReportsProperties, httpClient);
        FieldUtils.writeField(reportAuthorization, "userPrivileges",
                Collections.singletonList("userPrivilege"), true);

        Report report = mock(Report.class);
        when(report.getRequiredPrivilege()).thenReturn(null);

        // Mock static Reports.find method
        reportsMock.when(() -> Reports.find(eq("reportName"), any(), any())).thenReturn(report);

        // Execute
        boolean hasPrivilege = reportAuthorization.hasPrivilege("reportName");

        // Verify
        assertTrue(hasPrivilege);
        reportsMock.verify(() -> Reports.find(eq("reportName"), any(), any()));
    }
}