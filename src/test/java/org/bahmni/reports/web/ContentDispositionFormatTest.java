package org.bahmni.reports.web;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.scheduler.ReportsJob;
import org.bahmni.reports.scheduler.ReportsScheduler;
import org.bahmni.reports.web.security.OpenMRSAuthenticator;
import org.bahmni.webclients.HttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Characterisation tests run once per storable format (BAH-5317).
@RunWith(Parameterized.class)
public class ContentDispositionFormatTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final String responseType;
    private final String extension;

    public ContentDispositionFormatTest(String responseType, String extension) {
        this.responseType = responseType;
        this.extension = extension;
    }

    @Parameters(name = "{0} -> {1}")
    public static Collection<Object[]> formats() {
        return Arrays.asList(new Object[][]{
                {"application/pdf", ".pdf"},
                {"application/vnd.ms-excel", ".xlsx"},
                {"application/vnd.ms-excel-custom", ".xls"},
                {"text/csv", ".csv"},
                {"application/vnd.oasis.opendocument.spreadsheet", ".ods"},
        });
    }

    @Test
    public void reportDownloadAddsExactlyOneExtensionToReportName() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new JasperResponseConverter().applyHttpHeaders(responseType, response, "Visit Report");
        assertEquals("attachment; filename=\"Visit Report" + extension + "\"", response.getHeader("Content-Disposition"));
    }

    @Test
    public void fileExtensionIsMappedToFormat() {
        assertEquals(extension, JasperResponseConverter.getFileExtension(responseType));
    }

    @Test
    public void storedFileNameHasSingleExtensionAndNoSpacesOrSlashes() throws Exception {
        String storedFileName = generateStoredFileName("Visit Report/Summary");
        assertTrue(storedFileName, storedFileName.endsWith(extension));
        assertFalse(storedFileName, storedFileName.endsWith(extension + extension));
        assertTrue(storedFileName, storedFileName.startsWith("Visit_Report_Summary-"));
        assertFalse(storedFileName, storedFileName.contains(" ") || storedFileName.contains("/"));
    }

    @Test
    public void downloadHeaderNamesStoredFileWithSingleExtension() throws Exception {
        String storedFileName = generateStoredFileName("Visit Report");
        File savedFile = temporaryFolder.newFile();

        ScheduledReport scheduledReport = new ScheduledReport("id-1", "Visit Report", "superman",
                storedFileName, new Date(), new Date(), "Completed", responseType, new Date());
        ReportsScheduler reportsScheduler = mock(ReportsScheduler.class);
        when(reportsScheduler.getReportById("id-1")).thenReturn(scheduledReport);
        when(reportsScheduler.getFilePath(scheduledReport)).thenReturn(savedFile.getAbsolutePath());

        MainReportController controller = new MainReportController(new JasperResponseConverter(),
                mock(BahmniReportsProperties.class), mock(AllDatasources.class), mock(HttpClient.class),
                reportsScheduler, mock(OpenMRSAuthenticator.class));
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getScheduledReport("id-1", response);

        assertEquals("attachment; filename=\"" + storedFileName + "\"", response.getHeader("Content-Disposition"));
    }

    // The Content-Type must be on the ResponseEntity, because Tomcat does not report a Content-Type set on the
    // servlet response back to Spring (BAH-5317).
    @Test
    public void scheduledReportDownloadEntityCarriesContentType() throws Exception {
        ScheduledReport scheduledReport = new ScheduledReport("id-1", "Visit Report", "superman",
                generateStoredFileName("Visit Report"), new Date(), new Date(), "Completed", responseType, new Date());
        ReportsScheduler reportsScheduler = mock(ReportsScheduler.class);
        when(reportsScheduler.getReportById("id-1")).thenReturn(scheduledReport);
        when(reportsScheduler.getFilePath(scheduledReport)).thenReturn(temporaryFolder.newFile().getAbsolutePath());

        MainReportController controller = new MainReportController(new JasperResponseConverter(),
                mock(BahmniReportsProperties.class), mock(AllDatasources.class), mock(HttpClient.class),
                reportsScheduler, mock(OpenMRSAuthenticator.class));
        ResponseEntity<FileSystemResource> entity = controller.getScheduledReport("id-1", new MockHttpServletResponse());

        String expected = "application/vnd.ms-excel-custom".equals(responseType) ? "application/vnd.ms-excel" : responseType;
        assertEquals(expected, entity.getHeaders().getContentType().toString());
    }

    // ReportsJob.generateFileName() is private; it is the only place the stored name is built.
    private String generateStoredFileName(String reportName) throws Exception {
        ReportParams reportParams = new ReportParams();
        reportParams.setName(reportName);
        reportParams.setResponseType(responseType);
        ReportsJob job = new ReportsJob();
        job.setReportParams(reportParams);
        Method generateFileName = ReportsJob.class.getDeclaredMethod("generateFileName");
        generateFileName.setAccessible(true);
        return (String) generateFileName.invoke(job);
    }
}
