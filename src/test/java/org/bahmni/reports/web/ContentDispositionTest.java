package org.bahmni.reports.web;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.scheduler.ReportsScheduler;
import org.bahmni.reports.web.security.OpenMRSAuthenticator;
import org.bahmni.webclients.HttpClient;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Characterisation tests for Content-Disposition and file naming that do not depend on format (BAH-5317).
// Format-specific cases are in ContentDispositionFormatTest.
public class ContentDispositionTest {

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
        String storedFileName = "Visit_Report-2026-11-20_08:38:27.841_UTC.pdf";
        File savedFile = File.createTempFile("content-disposition", ".pdf");
        savedFile.deleteOnExit();
        Files.writeString(savedFile.toPath(), "%PDF-1.4 test", StandardCharsets.US_ASCII);

        ScheduledReport scheduledReport = new ScheduledReport("id-1", "Visit Report", "superman",
                storedFileName, new Date(), new Date(), "Completed", "application/pdf", new Date());
        ReportsScheduler reportsScheduler = mock(ReportsScheduler.class);
        when(reportsScheduler.getReportById("id-1")).thenReturn(scheduledReport);
        when(reportsScheduler.getFilePath(scheduledReport)).thenReturn(savedFile.getAbsolutePath());

        MainReportController controller = new MainReportController(new JasperResponseConverter(),
                mock(BahmniReportsProperties.class), mock(AllDatasources.class), mock(HttpClient.class),
                reportsScheduler, mock(OpenMRSAuthenticator.class));
        FileSystemResource resource = controller.getScheduledReport("id-1", new MockHttpServletResponse());

        assertEquals(savedFile.getAbsolutePath(), resource.getFile().getAbsolutePath());
        assertEquals("%PDF-1.4 test", Files.readString(resource.getFile().toPath(), StandardCharsets.US_ASCII));
    }
}
