package org.bahmni.reports.filter;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@Component
public class JasperResponseConverter {
    
    public void convert(String responseType, JasperReportBuilder report, HttpServletResponse response, final String fileName) throws IOException, DRException, SQLException {

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
                report.toXlsx(outputStream);
                break;
            case "application/pdf":
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".pdf");
                report.toPdf(outputStream);
        }
        //TODO: Mihir | Find better place to close the connection
    }
}
