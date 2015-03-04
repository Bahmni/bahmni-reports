package org.bahmni.reports.web;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@Controller
public class TestReportController {
    private static Logger logger = Logger.getLogger(TestReportController.class);

    @Autowired
    private DataSource dataSource;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public void printHello(HttpServletResponse response) throws Exception {
        try {
            writeExcelToResponse(response);
        } catch(Exception e) {
            logger.error(e);
            throw e;
        }
    }

    private void writeExcelToResponse(HttpServletResponse response) throws IOException, DRException, SQLException {
        JasperReportBuilder report = DynamicReports.report();

        report.columns(
                Columns.column("Identifier", "patient_id", DataTypes.integerType()),
                Columns.column("Created On", "date_created", DataTypes.dateType()),
                Columns.column("Created By", "creator", DataTypes.stringType())
        )
                .title(Components.text("Patient Listing").setHorizontalAlignment(HorizontalAlignment.CENTER))
                .setReportName("Test Report")
                .setDataSource("select * from patient limit 20", dataSource.getConnection());

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=test.xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        report.toXlsx(outputStream);

//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", "attachment; filename=test.pdf");
//        report.toPdf(outputStream);

        response.flushBuffer();
        outputStream.close();
    }
}