package org.bahmni.reports.filter;

import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.log4j.Logger;
import org.bahmni.reports.template.Templates;
import org.bahmni.reports.web.ReportParams;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

@Component
public class JasperResponseConverter {

    private static final Logger logger = Logger.getLogger(JasperResponseConverter.class);
    private static final String TEXT_HTML = "text/html";
    private static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    private static final String APPLICATION_VND_MS_EXCEL_CUSTOM = "application/vnd.ms-excel-custom";
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String TEXT_CSV = "text/csv";
    private static final String APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET = "application/vnd.oasis.opendocument.spreadsheet";

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
                JasperXlsExporterBuilder exporterBuilder = Exporters.xlsExporter(outputStream).setDetectCellType(true);
                exporterBuilder.setKeepWorkbookTemplateSheets(true);
                exporterBuilder.setWorkbookTemplate(reportParams.getMacroTemplateLocation());
                exporterBuilder.addSheetName("Report");
                concatenatedReportBuilder.toXls(exporterBuilder);
                File templateFile = new File(reportParams.getMacroTemplateLocation());
                if (reportParams.getMacroTemplateLocation().startsWith(macroTemplatesTempDirectory)) {
                    boolean delete = templateFile.delete();
                    if (!delete) {
                        logger.warn("Template file not deleted");
                    }
                }
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

    public void applyHttpHeaders(String responseType, HttpServletResponse response, String fileName) {
        switch (responseType) {
            case TEXT_HTML:
                response.setContentType(TEXT_HTML);
                break;
            case APPLICATION_VND_MS_EXCEL:
                response.setContentType(APPLICATION_VND_MS_EXCEL);
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
                break;
            case APPLICATION_VND_MS_EXCEL_CUSTOM:
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
                break;
            case APPLICATION_PDF:
                response.setContentType(APPLICATION_PDF);
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".pdf");
                break;
            case TEXT_CSV:
                response.setContentType(TEXT_CSV);
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".csv");
                break;
            case APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET:
                response.setContentType(APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET);
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".ods");
                break;
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
