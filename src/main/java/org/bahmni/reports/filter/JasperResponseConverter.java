package org.bahmni.reports.filter;

import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.template.Templates;
import org.bahmni.reports.util.LogSanitizer;
import org.bahmni.reports.web.ReportParams;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Component
public class JasperResponseConverter {

    private static final Logger logger = LogManager.getLogger(JasperResponseConverter.class);
    static final String TEXT_HTML = "text/html";
    static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    static final String APPLICATION_VND_MS_EXCEL_CUSTOM = "application/vnd.ms-excel-custom";
    static final String APPLICATION_VND_OPENXML_SPREADSHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static final String APPLICATION_PDF = "application/pdf";
    static final String TEXT_CSV = "text/csv";
    static final String APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET = "application/vnd.oasis.opendocument.spreadsheet";
    private static final String EX_INVALID_MACRO_TEMPLATE = "Invalid Template";

    public void convertToResponseType(ReportParams reportParams,
                                      String macroTemplatesTempDirectory, OutputStream outputStream,
                                      JasperConcatenatedReportBuilder concatenatedReportBuilder) throws DRException {
        switch (reportParams.getResponseType()) {
            case TEXT_HTML:
                concatenatedReportBuilder.toHtml(outputStream);
                break;
            case APPLICATION_VND_MS_EXCEL:
                concatenatedReportBuilder.toXlsx(outputStream);
                break;
            case APPLICATION_VND_MS_EXCEL_CUSTOM:
                // below code is to embed existing template and generate a new excel in xls format
                Path macroTemplateFile = macroTemplateFilePath(macroTemplatesTempDirectory, reportParams.getMacroTemplateLocation());
                File templateFile = macroTemplateFile.toFile();
                if (!templateFile.exists()) {
                    logger.error("Invalid Macro Template specified: {}", LogSanitizer.sanitize(macroTemplateFile.toString()));
                    throw new RuntimeException(EX_INVALID_MACRO_TEMPLATE);
                }
                JasperXlsExporterBuilder exporterBuilder = Exporters.xlsExporter(outputStream).setDetectCellType(true);
                exporterBuilder.setKeepWorkbookTemplateSheets(true);
                exporterBuilder.setWorkbookTemplate(macroTemplateFile.toString());
                exporterBuilder.addSheetName("Report");
                concatenatedReportBuilder.toXls(exporterBuilder);
                break;
            case APPLICATION_PDF:
                concatenatedReportBuilder.toPdf(outputStream);
                break;
            case TEXT_CSV:
                concatenatedReportBuilder.toCsv(outputStream);
                break;
            case APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET:
                concatenatedReportBuilder.toOds(outputStream);
                break;
        }
    }

    private Path macroTemplateFilePath(String macroTemplatesTempDirectory, String macroTemplate) {
        return Paths.get(macroTemplatesTempDirectory, macroTemplate);
    }

    public void applyReportTemplates(List<JasperReportBuilder> reports, String responseType) {
        for (JasperReportBuilder report : reports) {
            switch (responseType) {
                case TEXT_HTML, APPLICATION_PDF:
                    report.pageFooter(Templates.footerComponent);
                    break;
                case APPLICATION_VND_MS_EXCEL, APPLICATION_VND_MS_EXCEL_CUSTOM, TEXT_CSV,
                     APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET:
                    report.setTemplate(Templates.excelReportTemplate);
                    break;
            }
        }
    }

    /**
     * Sets the Content-Type and Content-Disposition headers for a report download.
     *
     * <p>The extension for {@code responseType} is appended to {@code fileName}, so pass the name
     * without an extension. Passing a name that already ends with the extension produces a doubled
     * extension (for example {@code report.pdf.pdf}).
     *
     * <p>Formats without a matching case (for example {@code text/html}, or an unknown type) set no
     * Content-Disposition header. HTML only gets its Content-Type.
     *
     * @param responseType the MIME type of the response, one of the constants in this class, such as
     *                     {@code application/pdf}. Selects the Content-Type and the extension to append.
     * @param response     the response whose headers are set. Its Content-Type and Content-Disposition
     *                     are set for every format that has an extension.
     * @param fileName     the download file name without an extension, for example
     *                     {@code Visit Report}. It is quoted and escaped
     *                     (for example {@code Bad"Name} becomes {@code "Bad\"Name"}) in the
     *                     Content-Disposition header.
     */
    public void applyHttpHeaders(String responseType, HttpServletResponse response, String fileName) {
        Optional<DownloadFormat> format = DownloadFormat.fromRequestType(responseType);
        if (format.isEmpty()) {
            return;
        }
        response.setContentType(format.get().contentType());
        if (format.get().isAttachment()) {
            response.setHeader("Content-Disposition", contentDisposition(fileName + format.get().extension()));
        }
    }

    private static String contentDisposition(String fileName) {
        return ContentDisposition.attachment().filename(fileName).build().toString();
    }

    /**
     * Returns {@code fileName} without the extension that {@link #applyHttpHeaders} appends for
     * {@code responseType}, so the result can be passed to it without a doubled extension. A name
     * that does not end with that extension is returned unchanged.
     */
    public static String stripFileExtension(String fileName, String responseType) {
        return Strings.CS.removeEnd(fileName, getFileExtension(responseType));
    }

    /**
     * Returns the Content-Type for {@code responseType}, or {@code null} for a format without one.
     * Custom Excel is served as {@code application/vnd.ms-excel}. The {@code .xlsx} format
     * ({@code application/vnd.ms-excel} as a request value) is served as the Open XML spreadsheet type.
     */
    public static String getContentType(String responseType) {
        return DownloadFormat.fromRequestType(responseType).map(DownloadFormat::contentType).orElse(null);
    }

    public static String getFileExtension(String responseType) {
        return DownloadFormat.fromRequestType(responseType).map(DownloadFormat::extension).orElse("");
    }

    /**
     * The report formats a client can request, and what each one is served as.
     *
     * <p>The request key is the {@code responseType} sent by the client. It is not always the served
     * type: a request for {@code application/vnd.ms-excel} is served as {@code .xlsx} with the Open XML
     * spreadsheet Content-Type, and a request for {@code application/vnd.ms-excel-custom} is served as
     * {@code .xls} with the legacy Excel Content-Type.
     */
    enum DownloadFormat {

        HTML(TEXT_HTML, TEXT_HTML, ".html", false),
        XLSX(APPLICATION_VND_MS_EXCEL, APPLICATION_VND_OPENXML_SPREADSHEET, ".xlsx", true),
        CUSTOM_XLS(APPLICATION_VND_MS_EXCEL_CUSTOM, APPLICATION_VND_MS_EXCEL, ".xls", true),
        PDF(APPLICATION_PDF, APPLICATION_PDF, ".pdf", true),
        CSV(TEXT_CSV, TEXT_CSV, ".csv", true),
        ODS(APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET, APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET, ".ods", true);

        private final String requestType;
        private final String contentType;
        private final String extension;
        private final boolean attachment;

        DownloadFormat(String requestType, String contentType, String extension, boolean attachment) {
            this.requestType = requestType;
            this.contentType = contentType;
            this.extension = extension;
            this.attachment = attachment;
        }

        String contentType() {
            return contentType;
        }

        String extension() {
            return extension;
        }

        /**
         * Whether the format is sent with a Content-Disposition header. HTML is not: it is shown in the
         * browser rather than saved.
         */
        boolean isAttachment() {
            return attachment;
        }

        /**
         * Returns the format for a client's {@code responseType}, or empty for an unknown or null value.
         */
        static Optional<DownloadFormat> fromRequestType(String requestType) {
            for (DownloadFormat format : values()) {
                if (format.requestType.equals(requestType)) {
                    return Optional.of(format);
                }
            }
            return Optional.empty();
        }
    }
}
