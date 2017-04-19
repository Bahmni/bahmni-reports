package org.bahmni.reports.web;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.scheduler.ReportsScheduler;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.WebClientsException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

@RestController
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
            logger.error("Scheduling report failed", e);
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getReports", method = RequestMethod.GET)
    public List<ScheduledReport> getReports(@RequestParam(name = "user") String user) {
        return reportsScheduler.getReports(user);
    }

    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    @ResponseBody
    public FileSystemResource getScheduledReport(@PathVariable("id") String id, HttpServletResponse httpServletResponse) {
        ScheduledReport scheduledReport = reportsScheduler.getReportById(id);
        converter.applyHttpHeaders(scheduledReport.getFormat(), httpServletResponse, scheduledReport.getFileName());
        return new FileSystemResource(reportsScheduler.getFilePath(scheduledReport));
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public void delete(@PathVariable("id") String id, HttpServletResponse httpServletResponse) {
        try {
            reportsScheduler.deleteScheduledReport(id);
            httpServletResponse.setStatus(200);
        } catch (Throwable e) {
            logger.error("Deleting report failed", e);
            httpServletResponse.setStatus(500);
        }
    }

    private static void catchBlock(HttpServletResponse response, Throwable e) {
        response.reset();
        e.printStackTrace();
        logger.error("Error running report", e);
        try {
            String errorMessage = e.getMessage();
            String content = "Incorrect Configuration " + errorMessage + "";
            if( e instanceof WebClientsException) {
                content = "<h3>" + errorMessage + "</h3>";
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, content);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
