package org.bahmni.reports.web;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Reports;
import org.bahmni.reports.scheduler.ReportsScheduler;
import org.bahmni.reports.web.security.OpenMRSAuthenticator;
import org.bahmni.reports.web.security.Privilege;
import org.bahmni.reports.web.security.Privileges;
import org.bahmni.webclients.HttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.bahmni.reports.web.security.AuthenticationFilter.REPORTING_COOKIE_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Reports.class, MainReportController.class})
public class MainReportControllerTest {

    private static final String SESSION_ID = "sessionId";

    @Mock
    private JasperResponseConverter converter;

    @Mock
    private BahmniReportsProperties bahmniReportsProperties;

    @Mock
    private AllDatasources allDatasources;

    @Mock
    private HttpClient httpClient;

    @Mock
    private ReportsScheduler reportsScheduler;

    @Mock
    private OpenMRSAuthenticator openMRSAuthenticator;

    @Mock
    private HttpServletRequest request;

    private MainReportController controller;

    private void mockCookie() {
        Cookie cookie = new Cookie(REPORTING_COOKIE_NAME, SESSION_ID);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
    }

    private Privilege buildPrivilege(String name) throws IllegalAccessException {
        Privilege privilege = new Privilege();
        FieldUtils.writeField(privilege, "name", name, true);
        return privilege;
    }

    @Test
    public void shouldNotGenerateReportWhenUserLacksRequiredPrivilege() throws Exception {
        controller = new MainReportController(converter, bahmniReportsProperties, allDatasources,
                httpClient, reportsScheduler, openMRSAuthenticator);
        mockCookie();

        Privileges privileges = new Privileges();
        privileges.add(buildPrivilege("SomeOtherPrivilege"));
        when(openMRSAuthenticator.callOpenMRS(SESSION_ID)).thenReturn(ResponseEntity.ok(privileges));

        Report report = mock(Report.class);
        when(report.getRequiredPrivilege()).thenReturn("RequiredPrivilege");
        mockStatic(Reports.class);
        when(Reports.find(any(), any(), any())).thenReturn(report);

        ReportParams reportParams = new ReportParams();
        reportParams.setName("someReport");

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.getReport(reportParams, response, request);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Privileges is required to access report", response.getErrorMessage());
        assertEquals(0, response.getContentAsByteArray().length);
        verify(converter, never()).applyHttpHeaders(any(), any(), any());
    }

    @Test
    public void shouldProceedToReportGenerationWhenUserHasRequiredPrivilege() throws Exception {
        controller = new MainReportController(converter, bahmniReportsProperties, allDatasources,
                httpClient, reportsScheduler, openMRSAuthenticator);
        mockCookie();

        Privileges privileges = new Privileges();
        when(openMRSAuthenticator.callOpenMRS(SESSION_ID)).thenReturn(ResponseEntity.ok(privileges));

        Report report = mock(Report.class);
        when(report.getRequiredPrivilege()).thenReturn(null);
        mockStatic(Reports.class);
        when(Reports.find(any(), any(), any())).thenReturn(report);

        ReportParams reportParams = new ReportParams();
        reportParams.setName("someReport");
        reportParams.setResponseType("text/html");

        MockHttpServletResponse response = new MockHttpServletResponse();

        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        whenNew(ReportGenerator.class).withAnyArguments().thenReturn(reportGenerator);

        controller.getReport(reportParams, response, request);

        verify(converter).applyHttpHeaders(anyString(), any(HttpServletResponse.class), anyString());
        verify(reportGenerator).invoke();
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
}
