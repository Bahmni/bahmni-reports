package org.bahmni.reports.filter;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bahmni.reports.web.ReportParams;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/*
 * Exercises JasperResponseConverter directly, without Spring or a database. The report builders
 * are real DynamicReports builders, so the assertions cover the actual export path (including
 * the OpenPDF PDF backend and the embedded default font in dynamicreports-defaults.xml).
 */
public class JasperResponseConverterTest {

    private static final String APPLICATION_PDF = "application/pdf";
    private static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    private static final String APPLICATION_VND_MS_EXCEL_CUSTOM = "application/vnd.ms-excel-custom";
    private static final String TEXT_HTML = "text/html";
    private static final String TEXT_CSV = "text/csv";
    private static final String UNICODE_MARKER = "unicode-marker";

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final JasperResponseConverter converter = new JasperResponseConverter();

    public static class ReportRow {
        private final String name;

        public ReportRow(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /*
     * AC1: guards the dependency graph from the test classpath's point of view. The packaged WAR
     * must be inspected separately (mvn dependency:tree / unzip + find, per the AC); this test
     * fails loudly if an exclusion is ever replaced by a version pin again.
     */
    @Test
    public void shouldNotShipItextAndShouldUseOpenPdf() {
        ClassLoader classLoader = getClass().getClassLoader();
        assertNull(classLoader.getResource("com/itextpdf/text/pdf/PdfWriter.class"));
        assertNotNull(classLoader.getResource("com/lowagie/text/pdf/PdfWriter.class"));
    }

    @Test
    public void pdfExportShouldProduceOpenablePdfWithAttachmentHeaders() throws Exception {
        byte[] pdf = convert(responseTypeParams(APPLICATION_PDF), concatenate(reportWith("pdf-marker", "Name")));

        assertTrue(pdf.length > 0);
        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
        assertEquals(1, pageCount(pdf));

        MockHttpServletResponse response = new MockHttpServletResponse();
        converter.applyHttpHeaders(APPLICATION_PDF, response, "pdf-check");
        assertEquals(APPLICATION_PDF, response.getContentType());
        assertEquals("attachment; filename=pdf-check.pdf", response.getHeader("Content-Disposition"));
    }

    @Test
    public void csvHtmlAndXlsxExportShouldBeUnaffected() throws Exception {
        String csv = new String(convert(responseTypeParams(TEXT_CSV), concatenate(reportWith("csv-marker", "Name"))),
                StandardCharsets.UTF_8);
        assertTrue(csv.contains("csv-marker"));
        assertTrue(csv.contains("Name"));

        String html = new String(convert(responseTypeParams(TEXT_HTML), concatenate(reportWith("html-marker", "Name"))),
                StandardCharsets.UTF_8);
        assertTrue(html.contains("html-marker"));

        byte[] xlsxBytes = convert(responseTypeParams(APPLICATION_VND_MS_EXCEL),
                concatenate(reportWith("xlsx-marker", "Name")));
        try (XSSFWorkbook xlsx = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            assertTrue(workbookContains(xlsx, "xlsx-marker"));
        }
    }

    @Test
    public void customXlsExportShouldBeUnaffected() throws Exception {
        File template = temporaryFolder.newFile("macro-template.xls");
        HSSFWorkbook emptyTemplate = new HSSFWorkbook();
        try (FileOutputStream outputStream = new FileOutputStream(template)) {
            emptyTemplate.write(outputStream);
        } finally {
            emptyTemplate.close();
        }

        ReportParams params = responseTypeParams(APPLICATION_VND_MS_EXCEL_CUSTOM);
        params.setMacroTemplateLocation(template.getName());
        byte[] xlsBytes = convert(params, concatenate(reportWith("xls-marker", "Name")),
                temporaryFolder.getRoot().getAbsolutePath());

        try (HSSFWorkbook xls = new HSSFWorkbook(new ByteArrayInputStream(xlsBytes))) {
            assertNotNull(xls.getSheet("Report"));
            assertTrue(workbookContains(xls, "xls-marker"));
        }
    }

    @Test
    public void concatenatedPdfShouldContainBothReportsInOrder() throws Exception {
        JasperReportBuilder first = titledReport("First sub report", "first-marker");
        JasperReportBuilder second = titledReport("Second sub report", "second-marker");

        byte[] pdf = convert(responseTypeParams(APPLICATION_PDF),
                concatenatedReport().concatenate(first, second));

        assertEquals(2, pageCount(pdf));
        String text = extractText(pdf);
        assertTrue(text.contains("first-marker"));
        assertTrue(text.contains("second-marker"));
        assertTrue(text.indexOf("First sub report") < text.indexOf("Second sub report"));
    }

    @Test
    public void emptyResultSetShouldProduceValidPdfWithHeader() throws Exception {
        JasperReportBuilder empty = report()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
                .columns(col.column("Name", "name", type.stringType()))
                .title(cmp.text("Empty report header"));
        empty.setDataSource(new ArrayList<ReportRow>());

        byte[] pdf = convert(responseTypeParams(APPLICATION_PDF), concatenate(empty));

        assertTrue(pdf.length > 0);
        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
        assertEquals(1, pageCount(pdf));
        assertTrue(extractText(pdf).contains("Empty report header"));
    }

    @Test
    public void nonLatinContentShouldRenderWithEmbeddedFont() throws Exception {
        List<ReportRow> rows = new ArrayList<ReportRow>();
        rows.add(new ReportRow(UNICODE_MARKER + " Журнал Ω مرحبا नमस्ते 中文 שלום"));

        JasperReportBuilder unicode = report()
                .columns(col.column("مرحبا", "name", type.stringType()))
                .title(cmp.text("Журнал Ω 中文").setStyle(stl.style().bold()));
        unicode.setDataSource(rows);

        String text = extractText(convert(responseTypeParams(APPLICATION_PDF), concatenate(unicode)));

        assertTrue(text.contains("Журнал"));
        assertTrue(text.contains("Ω"));
        assertTrue("Arabic glyphs should be present, not dropped", containsArabicGlyph(text));
        assertTrue("Devanagari glyphs should be present, not dropped", text.contains("नमस्ते"));
        assertTrue("CJK glyphs should be present, not dropped", text.contains("中文"));
        assertTrue("Hebrew glyphs should be present, not dropped", containsHebrewGlyph(text));
    }

    private JasperReportBuilder reportWith(String marker, String columnTitle) {
        List<ReportRow> rows = new ArrayList<ReportRow>();
        rows.add(new ReportRow(marker));
        JasperReportBuilder report = report().columns(col.column(columnTitle, "name", type.stringType()));
        report.setDataSource(rows);
        return report;
    }

    private JasperReportBuilder titledReport(String title, String marker) {
        List<ReportRow> rows = new ArrayList<ReportRow>();
        rows.add(new ReportRow(marker));
        JasperReportBuilder report = report()
                .columns(col.column("Name", "name", type.stringType()))
                .title(cmp.text(title));
        report.setDataSource(rows);
        return report;
    }

    private JasperConcatenatedReportBuilder concatenate(JasperReportBuilder report) {
        return concatenatedReport().concatenate(report);
    }

    private ReportParams responseTypeParams(String responseType) {
        ReportParams params = new ReportParams();
        params.setResponseType(responseType);
        params.setName("converter-test");
        return params;
    }

    private byte[] convert(ReportParams params, JasperConcatenatedReportBuilder builder) throws Exception {
        return convert(params, builder, temporaryFolder.getRoot().getAbsolutePath());
    }

    private byte[] convert(ReportParams params, JasperConcatenatedReportBuilder builder, String tempDirectory)
            throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        converter.convertToResponseType(params, tempDirectory, outputStream, builder);
        return outputStream.toByteArray();
    }

    private int pageCount(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            return reader.getNumberOfPages();
        }
    }

    private String extractText(byte[] pdf) throws Exception {
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder text = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                text.append(extractor.getTextFromPage(page)).append('\n');
            }
            return text.toString();
        }
    }

    private boolean containsArabicGlyph(String text) {
        return text.codePoints().anyMatch(codePoint -> {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
            return block == Character.UnicodeBlock.ARABIC
                    || block == Character.UnicodeBlock.ARABIC_SUPPLEMENT
                    || block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A
                    || block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B;
        });
    }

    private boolean containsHebrewGlyph(String text) {
        return text.codePoints().anyMatch(
                codePoint -> Character.UnicodeBlock.of(codePoint) == Character.UnicodeBlock.HEBREW);
    }

    private boolean workbookContains(Workbook workbook, String marker) {
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains(marker)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
