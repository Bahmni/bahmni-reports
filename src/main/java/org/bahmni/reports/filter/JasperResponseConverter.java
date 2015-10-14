package org.bahmni.reports.filter;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.Exporters;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Logger;
import org.bahmni.reports.template.Templates;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@Component
public class JasperResponseConverter {

    private static final Logger logger = Logger.getLogger(JasperResponseConverter.class);

    public void convert(String responseType, JasperReportBuilder report, HttpServletResponse response, final String fileName, String macroTemplateLocation) throws
            IOException, DRException, SQLException, JRException {

        response.setContentType("application/vnd.ms-excel");
        ServletOutputStream outputStream = response.getOutputStream();
        switch (responseType) {
            case "text/html":
                response.setContentType("text/html");
                report.toHtml(outputStream);
                break;
            case "application/vnd.ms-excel":
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
                report.setTemplate(Templates.excelReportTemplate);
                report.toXlsx(outputStream);
                break;
            case "application/vnd.ms-excel-custom":
                // below code is to embed existing template and generate a new excel in xls format
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
                report.setTemplate(Templates.excelReportTemplate);
                JasperXlsExporterBuilder exporterBuilder = Exporters.xlsExporter(outputStream).setDetectCellType(true);
                exporterBuilder.setKeepWorkbookTemplateSheets(true);
                exporterBuilder.setWorkbookTemplate(macroTemplateLocation);
                exporterBuilder.addSheetName("Report");
                report.toXls(exporterBuilder);
                File templateFile = new File(macroTemplateLocation);
                boolean delete = templateFile.delete();
                if(!delete){
                    logger.warn("Template file not deleted");
                }
                break;
            case "application/pdf":
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".pdf");
                report.toPdf(outputStream);
                break;
            case "text/csv":
                response.setContentType("text/csv");
                report.toCsv(outputStream);
                break;
        }
    }
}
