package org.bahmni.reports.controller;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.log4j.Logger;
import org.bahmni.reports.JasperResponseConverter;
import org.bahmni.reports.api.Reports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@Controller
public class BaseReportController {

    private final String baseUrl = "/bahmni-reports";
    private static final Logger logger = Logger.getLogger(BaseReportController.class);
    private Reports reports;
    private JasperResponseConverter converter;

    @Autowired
    public BaseReportController(Reports reports, JasperResponseConverter converter) {
        this.reports = reports;
        this.converter = converter;
    }

    @RequestMapping(value = baseUrl + "/report/{reportName}", method = RequestMethod.POST)
    public void getReport(@PathVariable String reportName, HttpServletRequest request, HttpServletResponse response) {
        JasperReportBuilder reportBuilder = null;
        try {
            reportBuilder = reports.findReport(reportName).run();
        } catch (SQLException e) {
            logger.error("Error running report", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        String acceptHeader = request.getHeader("Accept");
        convertToResponse(acceptHeader, reportBuilder, response);
    }

    private void convertToResponse(String acceptHeader, JasperReportBuilder reportBuilder, HttpServletResponse response) {
        try {
            converter.convert(acceptHeader, reportBuilder, response);
        } catch (DRException e) {
            logger.error("Could not convert response", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            logger.error("Could not convert response", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
