package org.bahmni.reports.scheduler;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bahmni.reports.scheduler.ReportStatus.COMPLETED;
import static org.bahmni.reports.scheduler.ReportStatus.ERROR;
import static org.bahmni.reports.scheduler.ReportStatus.PROCESSING;

public class ReportsJob implements Job {
    private static final Logger logger = Logger.getLogger(ReportsJob.class);

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
        String reportId = null;
        try {
            reportId = jobExecutionContext.getJobDetail().getKey().getName();
            logger.info("Running report " + reportId);
            final String fileName = generateFileName();
            ScheduledReport scheduledReport = scheduledReportRepository.findScheduledReportById(reportId);
            scheduledReport.setFileName(fileName);
            scheduledReport.setStatus(PROCESSING);

            File file = new File(bahmniReportsProperties.getReportsSaveDirectory(), fileName);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            scheduledReportRepository.save(scheduledReport);


            ReportGenerator reportGenerator = new ReportGenerator(reportParams, outputStream, allDatasources, bahmniReportsProperties, httpClient, jasperResponseConverter);
            reportGenerator.invoke();

            scheduledReport.setStatus(COMPLETED);
            scheduledReportRepository.save(scheduledReport);
            logger.info("Successfully ran report " + reportId);
        } catch (Throwable throwable) {
            logger.error("Error in running report " + reportId, throwable);
            ScheduledReport scheduledReport = scheduledReportRepository.findScheduledReportById(reportId);
            scheduledReport.setStatus(ERROR);
            scheduledReport.setErrorMessage(throwable.getCause().getMessage());
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
