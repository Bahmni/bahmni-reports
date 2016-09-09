package org.bahmni.reports.web;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.scheduler.ReportsScheduler;
import org.bahmni.webclients.HttpClient;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

@Controller
public class MainReportController {

    private static final Logger logger = Logger.getLogger(MainReportController.class);
    private JasperResponseConverter converter;
    private BahmniReportsProperties bahmniReportsProperties;
    private AllDatasources allDatasources;
    private HttpClient httpClient;
    private ReportsScheduler reportsScheduler;

    @Autowired
    public MainReportController(JasperResponseConverter converter,
                                BahmniReportsProperties bahmniReportsProperties,
                                AllDatasources allDatasources, HttpClient httpClient, ReportsScheduler reportsScheduler) {
        this.converter = converter;
        this.bahmniReportsProperties = bahmniReportsProperties;
        this.allDatasources = allDatasources;
        this.httpClient = httpClient;
        this.reportsScheduler = reportsScheduler;
    }

    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public void getReport(ReportParams reportParams, HttpServletResponse response) {
        try {
            converter.applyHttpHeaders(reportParams.getResponseType(), response, reportParams.getName());
            ReportGenerator reportGenerator = new ReportGenerator(reportParams, response.getOutputStream(), allDatasources, bahmniReportsProperties, httpClient, converter);
            reportGenerator.invoke();
        } catch (Throwable e) {
            catchBlock(response, e);
        }
    }

    @RequestMapping(value = "/schedule", method = RequestMethod.GET)
    public void schedule(ReportParams reportParams, HttpServletResponse response) {
        try {
            reportsScheduler.schedule(reportParams);
        } catch (ParseException | UnsupportedEncodingException e) {
            catchBlock(response, e);
        } catch (SchedulerException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static void catchBlock(HttpServletResponse response, Throwable e) {
        e.printStackTrace();
        logger.error("Error running report", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try {
            String errorMessage = e.getMessage();
            String content = "<h2>Incorrect Configuration</h2><h3>" + errorMessage + "</h3>";
            response.setContentLength(content.length());
            response.setContentType("text/html");
            response.getOutputStream().write(content.getBytes());
            response.flushBuffer();
            response.getOutputStream().close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
