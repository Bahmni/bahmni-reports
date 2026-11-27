package org.bahmni.reports.web.realcontainer;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.scheduler.ReportsScheduler;
import org.bahmni.reports.web.MainReportController;
import org.bahmni.reports.web.security.OpenMRSAuthenticator;
import org.bahmni.reports.web.security.Privilege;
import org.bahmni.reports.web.security.Privileges;
import org.bahmni.webclients.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * BAH-5100 QA feedback: the production API was reported to always return 200 for a report
 * request denied for lack of privilege. This test's job is to verify getReport()'s denial path
 * against a real servlet container rather than MockHttpServletResponse, because the two behave
 * differently at the point that matters here: MockHttpServletResponse.sendError() commits the
 * response synchronously, while the real container's
 * org.apache.catalina.connector.Response.sendError() only clears the buffer and marks the
 * response "suspended" (later writes become silent no-ops rather than throwing) -- the actual
 * status that reaches the client is decided afterwards by the host pipeline. A mock-only test
 * cannot observe that difference at all.
 *
 * This test drives getReport() through org.apache.catalina.startup.Tomcat, the same minimal
 * programmatic bootstrap org.bahmni.deployment.launch.Main (bundled inside
 * bahmni-embedded-tomcat-8.0.42.jar, downloaded in the Dockerfile) uses to start production.
 * tomcat-embed-core:8.0.42 was checked against that production jar by hashing the compiled
 * classes: Response, OutputBuffer, ErrorReportValve, StandardHost and Tomcat are byte-for-byte
 * identical between the two, so this runs the exact container code path production runs, not an
 * approximation of it.
 *
 * What was actually confirmed here (see the PR description for the full investigation): with
 * the fix in place (PR #108's `return`, plus this change's 403), a denied request on this real
 * container returns 403 with no report content in the body, as expected. Separately, driving the
 * pre-PR#108 controller (sendError with no return) through this same real container -- both as a
 * bare servlet and through the full Spring MVC stack with AuthenticationFilter wired in, with
 * report generation actually succeeding against a real JDBC connection -- still produced 403, not
 * 200. So the real container is not, by itself, the source of the 200 QA observed; whatever turns
 * this into 200 sits somewhere this repository does not control (most likely the reverse proxy in
 * front of bahmni-reports in a full Bahmni deployment, e.g. the bahmni-web image in
 * bahmni-docker, which is out of scope here). This test therefore documents and locks in the
 * correct denial behaviour at the container level; it does not reproduce the QA-observed 200,
 * because that could not be reproduced at this layer.
 */
public class MainReportControllerEmbeddedTomcatTest {

    private static final String REPORT_NAME = "someReport";
    private static final String REQUIRED_PRIVILEGE = "RequiredPrivilege";
    private static final String REPORTING_COOKIE_NAME = "reporting_session";
    private static final String SESSION_ID = "sessionId";

    private Tomcat tomcat;
    private int port;
    private OpenMRSAuthenticator openMRSAuthenticator;

    @Before
    public void startRealContainer() throws Exception {
        openMRSAuthenticator = mock(OpenMRSAuthenticator.class);

        JasperResponseConverter converter = new JasperResponseConverter();
        BahmniReportsProperties bahmniReportsProperties = mock(BahmniReportsProperties.class);
        AllDatasources allDatasources = mock(AllDatasources.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.get(any(URI.class))).thenReturn(reportsConfigJson());
        when(bahmniReportsProperties.getConfigFileUrl()).thenReturn("http://example.invalid/reports.json");
        ReportsScheduler reportsScheduler = mock(ReportsScheduler.class);

        MainReportController controller = new MainReportController(converter, bahmniReportsProperties,
                allDatasources, httpClient, reportsScheduler, openMRSAuthenticator);

        File docBase = Files.createTempDirectory("bah-5100-embedded-tomcat").toFile();
        docBase.deleteOnExit();

        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir(docBase.getAbsolutePath());

        Context context = tomcat.addContext("", docBase.getAbsolutePath());
        Tomcat.addServlet(context, "report", new ReportServlet(controller)).addMapping("/report");

        tomcat.start();
        port = tomcat.getConnector().getLocalPort();
    }

    @After
    public void stopRealContainer() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Test
    public void shouldReturnForbiddenWithNoBodyWhenUserLacksRequiredPrivilegeOnRealContainer() throws Exception {
        givenUserHasPrivileges("SomeOtherPrivilege");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:" + port + "/report?name=" + REPORT_NAME
                    + "&responseType=text/html");
            request.setHeader("Cookie", REPORTING_COOKIE_NAME + "=" + SESSION_ID);

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());

                /*
                 * With the fix (return + 403) in place, the real container reports the denial
                 * correctly and no report content leaks into the body. The body itself is
                 * Tomcat's own ErrorReportValve HTML page (see
                 * org.apache.catalina.valves.ErrorReportValve#report), not an empty response --
                 * that page is only reachable once sendError() has set status/message and the
                 * method has returned without writing anything else. The one thing that must
                 * never appear in it is report content, which is what would show up if
                 * ReportGenerator had run after the denial.
                 */
                assertEquals(403, statusCode);
                assertTrue("expected Tomcat's own error page naming the denial message, got: " + body,
                        body.contains("Privileges is required to access report"));
                assertTrue("report content must not appear in a denied response, got: " + body,
                        !body.contains(REPORT_NAME) && !body.toLowerCase().contains("patient"));
            }
        }
    }

    private void givenUserHasPrivileges(String... privilegeNames) throws IllegalAccessException {
        Privileges privileges = new Privileges();
        for (String name : privilegeNames) {
            Privilege privilege = new Privilege();
            FieldUtils.writeField(privilege, "name", name, true);
            privileges.add(privilege);
        }
        when(openMRSAuthenticator.callOpenMRS(SESSION_ID)).thenReturn(ResponseEntity.ok(privileges));
    }

    private static String reportsConfigJson() {
        return "{\"someReport\":{\"name\":\"" + REPORT_NAME + "\",\"type\":\"PatientReport\","
                + "\"requiredPrivilege\":\"" + REQUIRED_PRIVILEGE + "\"}}";
    }

    /*
     * Thinnest possible bridge from a real servlet container to the real controller method:
     * no Spring dispatch, no interceptors, so the only thing that can make this test diverge
     * from a mock-based unit test is the container's own Response/OutputBuffer behaviour.
     */
    private static class ReportServlet extends HttpServlet {
        private final MainReportController controller;

        ReportServlet(MainReportController controller) {
            this.controller = controller;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            org.bahmni.reports.web.ReportParams reportParams = new org.bahmni.reports.web.ReportParams();
            reportParams.setName(req.getParameter("name"));
            reportParams.setResponseType(req.getParameter("responseType"));
            controller.getReport(reportParams, resp, req);
        }
    }
}
