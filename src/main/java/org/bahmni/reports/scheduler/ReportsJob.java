package org.bahmni.reports.scheduler;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.bahmni.reports.util.BahmniReportUtil;
import org.bahmni.reports.web.ReportGenerator;
import org.bahmni.reports.web.ReportParams;
import org.bahmni.webclients.HttpClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReportsJob implements Job {

    public static final String PROCESSING = "Processing";
    public static final String COMPLETED = "Completed";
    public static final String ERROR = "Error";
    private ReportParams reportParams;

    @Autowired
    private AllDatasources allDatasources;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private BahmniReportsProperties bahmniReportsProperties;

    @Autowired
    private JasperResponseConverter jasperResponseConverter;

    @Autowired
    private ScheduledReportRepository scheduledReportRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            final String fileName = generateFileName();

            ScheduledReport scheduledReport = scheduledReportRepository.findScheduledReportById(jobExecutionContext.getJobDetail().getKey().getName());
            scheduledReport.setFileName(fileName);
            scheduledReport.setStatus(PROCESSING);
            scheduledReportRepository.save(scheduledReport);

            File file = new File(bahmniReportsProperties.getReportsSaveDirectory(), fileName);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            ReportGenerator reportGenerator = new ReportGenerator(reportParams, outputStream, allDatasources, bahmniReportsProperties, httpClient, jasperResponseConverter);
            reportGenerator.invoke();

            scheduledReport.setStatus(COMPLETED);
            scheduledReportRepository.save(scheduledReport);
        } catch (Throwable throwable) {
            ScheduledReport scheduledReport = scheduledReportRepository.findScheduledReportById(jobExecutionContext.getJobDetail().getKey().getName());
            scheduledReport.setStatus(ERROR);
            scheduledReport.setErrorMessage(BahmniReportUtil.getStackTrace(throwable));
            scheduledReportRepository.save(scheduledReport);
        }
    }

    private String generateFileName() throws UnsupportedEncodingException {
        Date date = new Date();
        String fileCreationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z").format(date);
        String fileName = reportParams.getName() + "-" +
                fileCreationDate +
                JasperResponseConverter.getFileExtension(reportParams.getResponseType());
        return fileName.replaceAll("[ /]", "_");
    }

    public void setReportParams(Object reportParams) {
        this.reportParams = (ReportParams) reportParams;
    }
}
