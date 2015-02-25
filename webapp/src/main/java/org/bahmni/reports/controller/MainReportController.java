package org.bahmni.reports.controller;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.log4j.Logger;
import org.bahmni.reports.JasperResponseConverter;
import org.bahmni.reports.api.ReportTemplates;
import org.bahmni.reports.api.model.ReportConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

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

    @RequestMapping(value = "/report", method = RequestMethod.POST)
    public void getReport(@RequestBody ReportConfig reportConfig, HttpServletRequest request, HttpServletResponse response) {
        JasperReportBuilder reportBuilder = null;
        try {
            reportBuilder = reportTemplates.get(reportConfig.getTemplateName()).build(reportConfig);
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
        } catch (DRException | IOException e) {
            logger.error("Could not convert response", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
