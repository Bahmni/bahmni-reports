package org.bahmni.reports.filter;

import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Logger;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.template.Templates;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport;

@Component
public class JasperResponseConverter {

    private static final Logger logger = Logger.getLogger(JasperResponseConverter.class);

    public void convert(String responseType, BahmniReportBuilder bahmniReportBuilder, HttpServletResponse response,
                        final String fileName, String macroTemplateLocation, String macroTemplatesTempDirectory)
            throws IOException, DRException, SQLException, JRException {

        response.setContentType("application/vnd.ms-excel");
        ServletOutputStream outputStream = response.getOutputStream();

        List<JasperReportBuilder> reports = bahmniReportBuilder.getReportBuilders();
        applyTemplatesToAllReports(reports, responseType, response, fileName);
        JasperConcatenatedReportBuilder concatenatedReportBuilder = concatenatedReport().concatenate(reports.toArray(new JasperReportBuilder[reports.size()]));

        convertToResponseType(responseType, macroTemplateLocation, macroTemplatesTempDirectory, outputStream, concatenatedReportBuilder);
    }

    private void convertToResponseType(String responseType, String macroTemplateLocation,
                                       String macroTemplatesTempDirectory, ServletOutputStream outputStream,
                                       JasperConcatenatedReportBuilder concatenatedReportBuilder) throws DRException {
        switch (responseType) {
            case "text/html":
                concatenatedReportBuilder.toHtml(outputStream);
                break;
            case "application/vnd.ms-excel":
                concatenatedReportBuilder.toXlsx(outputStream);
                break;
            case "application/vnd.ms-excel-custom":
                // below code is to embed existing template and generate a new excel in xls format
                JasperXlsExporterBuilder exporterBuilder = Exporters.xlsExporter(outputStream).setDetectCellType(true);
                exporterBuilder.setKeepWorkbookTemplateSheets(true);
                exporterBuilder.setWorkbookTemplate(macroTemplateLocation);
                exporterBuilder.addSheetName("Report");
                concatenatedReportBuilder.toXls(exporterBuilder);
                File templateFile = new File(macroTemplateLocation);
                if (macroTemplateLocation.startsWith(macroTemplatesTempDirectory)) {
                    boolean delete = templateFile.delete();
                    if (!delete) {
                        logger.warn("Template file not deleted");
                    }
                }
                break;
            case "application/pdf":
                concatenatedReportBuilder.toPdf(outputStream);
                break;
            case "text/csv":
                concatenatedReportBuilder.toCsv(outputStream);
                break;
            case "application/vnd.oasis.opendocument.spreadsheet":
                concatenatedReportBuilder.toOds(outputStream);
                break;
        }
    }

    private void applyTemplatesToAllReports(List<JasperReportBuilder> reports, String responseType, HttpServletResponse response, String fileName) {
        for (JasperReportBuilder report : reports) {
            switch (responseType) {
                case "text/html":
                    response.setContentType("text/html");
                    report.pageFooter(Templates.footerComponent);
                    break;
                case "application/vnd.ms-excel":
                    response.setContentType("application/vnd.ms-excel");
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
                    report.setTemplate(Templates.excelReportTemplate);
                    break;
                case "application/vnd.ms-excel-custom":
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
                    report.setTemplate(Templates.excelReportTemplate);
                    break;
                case "application/pdf":
                    response.setContentType("application/pdf");
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".pdf");
                    report.pageFooter(Templates.footerComponent);
                    break;
                case "text/csv":
                    response.setContentType("text/csv");
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".csv");
                    report.setTemplate(Templates.excelReportTemplate);
                    break;
                case "application/vnd.oasis.opendocument.spreadsheet":
                    response.setContentType("application/vnd.oasis.opendocument.spreadsheet");
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".ods");
                    report.setTemplate(Templates.excelReportTemplate);
                    break;
            }
        }

    }
}
