package org.bahmni.reports.web;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.scheduler.ReportsScheduler;
import org.bahmni.reports.web.security.OpenMRSAuthenticator;
import org.bahmni.webclients.HttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Characterisation tests for Content-Disposition and file naming that do not depend on format (BAH-5317).
// Format-specific cases are in ContentDispositionFormatTest.
public class ContentDispositionTest {

    private static final String STORED_FILE_NAME = "Visit_Report-2026-11-20_08:38:27.841_UTC.pdf";
    private static final String SAVED_CONTENT = "%PDF-1.4 test";

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void htmlSetsNoContentDisposition() {
        MockHttpServletResponse html = new MockHttpServletResponse();
        new JasperResponseConverter().applyHttpHeaders("text/html", html, "Visit Report");
        assertNull(html.getHeader("Content-Disposition"));
        assertEquals("text/html", html.getContentType());
    }

    @Test
    public void unknownFormatSetsNoContentDisposition() {
        MockHttpServletResponse unknown = new MockHttpServletResponse();
        new JasperResponseConverter().applyHttpHeaders("application/unknown", unknown, "Visit Report");
        assertNull(unknown.getHeader("Content-Disposition"));
    }

    @Test
    public void unknownFormatHasNoExtension() {
        assertEquals("", JasperResponseConverter.getFileExtension("application/unknown"));
    }

    @Test
    public void scheduledReportDownloadReturnsTheSavedFile() throws IOException {
        FileSystemResource resource = downloadScheduledReport(new MockHttpServletResponse());

        assertEquals(SAVED_CONTENT, Files.readString(resource.getFile().toPath(), StandardCharsets.US_ASCII));
    }

    // Guards the run timestamp in the download name
    @Test
    public void scheduledReportDownloadKeepsRunTimestampInFileName() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        downloadScheduledReport(response);

        String header = response.getHeader("Content-Disposition");
        assertNotNull(header);
        assertTrue(header, header.contains("Visit_Report-2026-11-20_08:38:27.841_UTC"));
    }

    // Regression test for BAH-5317: the stored name already ends in .pdf, so the header must not add a second one.
    // Before the fix this header was "...UTC.pdf.pdf".
    @Test
    public void scheduledReportDownloadHeaderHasExactlyOneExtension() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        downloadScheduledReport(response);

        assertEquals("attachment; filename=" + STORED_FILE_NAME, response.getHeader("Content-Disposition"));
    }

    // End-to-end for an HTML scheduled report: the file is served, with a Content-Type and no Content-Disposition.
    @Test
    public void scheduledHtmlReportDownloadSetsContentTypeWithoutContentDisposition() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        FileSystemResource resource = downloadScheduledReport(response, "text/html", "Visit_Report-2026-11-20_08:38:27.841_UTC.html");

        assertNull(response.getHeader("Content-Disposition"));
        assertEquals("text/html", response.getContentType());
        assertEquals(SAVED_CONTENT, Files.readString(resource.getFile().toPath(), StandardCharsets.US_ASCII));
    }

    // Serves a saved report through the real controller and converter, and returns the file it serves.
    private FileSystemResource downloadScheduledReport(MockHttpServletResponse response) throws IOException {
        return downloadScheduledReport(response, "application/pdf", STORED_FILE_NAME);
    }

    private FileSystemResource downloadScheduledReport(MockHttpServletResponse response, String format,
                                                       String storedFileName) throws IOException {
        File savedFile = temporaryFolder.newFile();
        Files.writeString(savedFile.toPath(), SAVED_CONTENT, StandardCharsets.US_ASCII);

        ScheduledReport scheduledReport = new ScheduledReport("id-1", "Visit Report", "superman",
                storedFileName, new Date(), new Date(), "Completed", format, new Date());
        ReportsScheduler reportsScheduler = mock(ReportsScheduler.class);
        when(reportsScheduler.getReportById("id-1")).thenReturn(scheduledReport);
        when(reportsScheduler.getFilePath(scheduledReport)).thenReturn(savedFile.getAbsolutePath());

        MainReportController controller = new MainReportController(new JasperResponseConverter(),
                mock(BahmniReportsProperties.class), mock(AllDatasources.class), mock(HttpClient.class),
                reportsScheduler, mock(OpenMRSAuthenticator.class));
        return controller.getScheduledReport("id-1", response);
    }
}
