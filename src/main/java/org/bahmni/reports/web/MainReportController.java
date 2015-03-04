package org.bahmni.reports.web;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.log4j.Logger;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.ReportConfig;
import org.bahmni.reports.template.ReportTemplates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

import static org.bahmni.reports.util.ConfigReaderUtil.findConfig;


@Controller
public class MainReportController {

    private static final Logger logger = Logger.getLogger(MainReportController.class);
    private ReportTemplates reportTemplates;
    private JasperResponseConverter converter;

    @Autowired
    public MainReportController(ReportTemplates reportTemplates, JasperResponseConverter converter) {
        this.reportTemplates = reportTemplates;
        this.converter = converter;
    }

    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public void getReport(HttpServletRequest request, HttpServletResponse response) {
        try {
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String reportName = request.getParameter("name");

            ReportConfig reportConfig = findConfig(reportName);

            JasperReportBuilder reportBuilder = reportTemplates.get(reportConfig.getType()).
                    build(reportConfig, startDate, endDate);

            String responseType = request.getParameter("responseType");
            convertToResponse(responseType, reportBuilder, response, reportConfig.getName());

            response.flushBuffer();
            response.getOutputStream().close();
        } catch (SQLException | IOException e) {
            logger.error("Error running report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private void convertToResponse(String responseType, JasperReportBuilder reportBuilder, HttpServletResponse response, String fileName) {
        try {
            converter.convert(responseType, reportBuilder, response, fileName);
        } catch (DRException | IOException e) {
            logger.error("Could not convert response", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
