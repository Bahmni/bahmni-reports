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
import org.bahmni.reports.web.ReportParams;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class JasperResponseConverter {

    private static final Logger logger = LogManager.getLogger(JasperResponseConverter.class);
    private static final String TEXT_HTML = "text/html";
    private static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    private static final String APPLICATION_VND_MS_EXCEL_CUSTOM = "application/vnd.ms-excel-custom";
    private static final String APPLICATION_VND_OPENXML_SPREADSHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String TEXT_CSV = "text/csv";
    private static final String APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET = "application/vnd.oasis.opendocument.spreadsheet";
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
                    logger.error("Invalid Macro Template specified: {}", sanitizeForLog(macroTemplateFile.toString()));
                    throw new RuntimeException(EX_INVALID_MACRO_TEMPLATE);
                }
                JasperXlsExporterBuilder exporterBuilder = Exporters.xlsExporter(outputStream).setDetectCellType(true);
                exporterBuilder.setKeepWorkbookTemplateSheets(true);
                exporterBuilder.setWorkbookTemplate(macroTemplateFile.toString());
                exporterBuilder.addSheetName("Report");
                concatenatedReportBuilder.toXls(exporterBuilder);
                // boolean delete = templateFile.delete();
                // if (!delete) {
                //    logger.warn(String.format("Uploaded report template file not deleted: %s", macroTemplateFile));
                // }
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

    private String sanitizeForLog(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\\p{Cntrl}\\u2028\\u2029]", "_");
    }

    public void applyReportTemplates(List<JasperReportBuilder> reports, String responseType) {
        for (JasperReportBuilder report : reports) {
            switch (responseType) {
                case TEXT_HTML:
                    report.pageFooter(Templates.footerComponent);
                    break;
                case APPLICATION_VND_MS_EXCEL:
                    report.setTemplate(Templates.excelReportTemplate);
                    break;
                case APPLICATION_VND_MS_EXCEL_CUSTOM:
                    report.setTemplate(Templates.excelReportTemplate);
                    break;
                case APPLICATION_PDF:
                    report.pageFooter(Templates.footerComponent);
                    break;
                case TEXT_CSV:
                    report.setTemplate(Templates.excelReportTemplate);
                    break;
                case APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET:
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
        switch (responseType) {
            case TEXT_HTML:
                response.setContentType(TEXT_HTML);
                break;
            case APPLICATION_VND_MS_EXCEL:
                response.setContentType(APPLICATION_VND_OPENXML_SPREADSHEET);
                response.setHeader("Content-Disposition", contentDisposition(fileName + ".xlsx"));
                break;
            case APPLICATION_VND_MS_EXCEL_CUSTOM:
                response.setContentType(APPLICATION_VND_MS_EXCEL);
                response.setHeader("Content-Disposition", contentDisposition(fileName + ".xls"));
                break;
            case APPLICATION_PDF:
                response.setContentType(APPLICATION_PDF);
                response.setHeader("Content-Disposition", contentDisposition(fileName + ".pdf"));
                break;
            case TEXT_CSV:
                response.setContentType(TEXT_CSV);
                response.setHeader("Content-Disposition", contentDisposition(fileName + ".csv"));
                break;
            case APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET:
                response.setContentType(APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET);
                response.setHeader("Content-Disposition", contentDisposition(fileName + ".ods"));
                break;
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
        switch (responseType) {
            case TEXT_HTML:
                return TEXT_HTML;
            case APPLICATION_VND_MS_EXCEL:
                return APPLICATION_VND_OPENXML_SPREADSHEET;
            case APPLICATION_VND_MS_EXCEL_CUSTOM:
                return APPLICATION_VND_MS_EXCEL;
            case APPLICATION_PDF:
                return APPLICATION_PDF;
            case TEXT_CSV:
                return TEXT_CSV;
            case APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET:
                return APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET;
            default:
                return null;
        }
    }

    public static String getFileExtension(String responseType) {
        switch (responseType) {
            case TEXT_HTML:
                return ".html";
            case APPLICATION_VND_MS_EXCEL:
                return ".xlsx";
            case APPLICATION_VND_MS_EXCEL_CUSTOM:
                return ".xls";
            case APPLICATION_PDF:
                return ".pdf";
            case TEXT_CSV:
                return ".csv";
            case APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET:
                return ".ods";
            default:
                return "";
        }
    }
}
