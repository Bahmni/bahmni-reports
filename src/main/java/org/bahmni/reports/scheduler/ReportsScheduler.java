package org.bahmni.reports.scheduler;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.bahmni.reports.web.ReportParams;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.bahmni.reports.scheduler.ReportStatus.COMPLETED;
import static org.bahmni.reports.scheduler.ReportStatus.ERROR;
import static org.bahmni.reports.scheduler.ReportStatus.PROCESSING;
import static org.bahmni.reports.scheduler.ReportStatus.QUEUED;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class ReportsScheduler {
    private static final Logger logger = Logger.getLogger(ReportsScheduler.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduledReportRepository scheduledReportRepository;

    @Autowired
    private BahmniReportsProperties bahmniReportsProperties;


    public void schedule(ReportParams reportParams) throws ParseException, SchedulerException, UnsupportedEncodingException {
        logger.info("Starting to schedule report " + reportParams.getName());

        JobDetail job = newJob(ReportsJob.class).build();
        job.getJobDataMap().put("reportParams", reportParams);

        Trigger trigger = newTrigger().startNow().build();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ScheduledReport scheduledReport = new ScheduledReport(job.getKey().getName(), reportParams.getName(), reportParams.getUserName(), null, simpleDateFormat.parse(reportParams.getStartDate()), simpleDateFormat.parse(reportParams.getEndDate()), QUEUED, reportParams.getResponseType(), new Date());
        scheduledReportRepository.save(scheduledReport);

        scheduler.scheduleJob(job, trigger);

        logger.info("Successfully scheduled report " + reportParams.getName());
    }

    public List<ScheduledReport> getReports(String user) {
        return scheduledReportRepository.findScheduledReportsByUser(user);
    }

    public static void scheduleCrons(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {

        BahmniReportsProperties bahmniReportsProperties = new BahmniReportsProperties();

        String triggerTime = bahmniReportsProperties.getCleanupJobTriggerTime();

        if (triggerTime == null) triggerTime = "0 0 0 * * ?";

        JobKey jobKey = new JobKey("cleanjob", "bahmnireports");

        if (schedulerFactoryBean.getScheduler().checkExists(jobKey)) {
            JobDetail existingJob = schedulerFactoryBean.getScheduler().getJobDetail(jobKey);
            String scheduleData = existingJob.getJobDataMap().get("scheduleData").toString();
            if (triggerTime.trim().equals(scheduleData.trim())) return;
            schedulerFactoryBean.getScheduler().interrupt(jobKey);
            schedulerFactoryBean.getScheduler().deleteJob(jobKey);
        }

        JobDetail cleanupJob = newJob(CleanReportsJob.class)
                .withIdentity(jobKey)
                .build();

        cleanupJob.getJobDataMap().put("scheduleData", triggerTime);

        Trigger cleanupTrigger;
        cleanupTrigger = newTrigger()
                .withSchedule(cronSchedule(triggerTime))
                .forJob(jobKey)
                .build();

        schedulerFactoryBean.getScheduler().scheduleJob(cleanupJob, cleanupTrigger);

        logger.info("Reports cleanup job scheduled for:" + triggerTime);
    }

    public String getFilePath(ScheduledReport scheduledReport) {
        return bahmniReportsProperties.getReportsSaveDirectory() + File.separator + scheduledReport.getFileName();
    }

    public ScheduledReport getReportById(String id) {
        return scheduledReportRepository.findScheduledReportById(id);
    }

    public void deleteScheduledReport(String id) throws SchedulerException {
        ScheduledReport scheduledReport = scheduledReportRepository.findScheduledReportById(id);
        switch (scheduledReport.getStatus()) {
            case PROCESSING:
                break;
            case QUEUED:
                scheduler.deleteJob(jobKey(id));
                scheduledReportRepository.delete(scheduledReport);
                logger.info("Deleted scheduled report job " + scheduledReport.getId());
                break;
            case ERROR:
            case COMPLETED:
                deleteFileOfReport(scheduledReport);
                scheduledReportRepository.delete(scheduledReport);
                logger.info("Deleted the completed report file " + scheduledReport.getFileName());
                break;
        }
    }

    private void deleteFileOfReport(ScheduledReport scheduledReport) {
        if (scheduledReport.getFileName() != null) {
            File file = new File(getFilePath(scheduledReport));
            file.delete();
        }
    }
}
