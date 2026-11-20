package org.bahmni.reports.web;

import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.scheduler.ReportsJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Characterisation tests run once per storable format (BAH-5317).
@RunWith(Parameterized.class)
public class ContentDispositionFormatTest {

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
        assertEquals("attachment; filename=Visit Report" + extension, response.getHeader("Content-Disposition"));
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
