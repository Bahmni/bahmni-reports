package org.bahmni.reports;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JasperResponseConverter {
    public void convert(String acceptHeader, JasperReportBuilder report, HttpServletResponse response) throws IOException, DRException {
        ServletOutputStream outputStream = response.getOutputStream();
        switch (acceptHeader) {
            case "text/html":
                response.setContentType("text/html");
                report.toHtml(outputStream);
                break;
            case "application/vnd.ms-excel":
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment; filename=Inpatient_Outcome.xlsx");
                report.toXlsx(outputStream);
                break;
            case "application/pdf":
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=test.pdf");
                report.toPdf(outputStream);
        }
    }
}
