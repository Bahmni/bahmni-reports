package org.bahmni.reports.scheduler;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.bahmni.reports.web.ReportParams;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class ReportsScheduler {
    public static final String QUEUED = "Queued";

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduledReportRepository scheduledReportRepository;

    private static final Logger logger = Logger.getLogger(ReportsScheduler.class);

    public ReportsScheduler() {
    }

    public void schedule(ReportParams reportParams) throws ParseException, SchedulerException, UnsupportedEncodingException {
        JobDetail job = newJob(ReportsJob.class).build();
        job.getJobDataMap().put("reportParams", reportParams);

        Trigger trigger = newTrigger().startNow().build();

        scheduler.scheduleJob(job, trigger);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy-mm-dd");
        ScheduledReport scheduledReport = new ScheduledReport(job.getKey().getName(), reportParams.getName(), reportParams.getUserName(), null, simpleDateFormat.parse(reportParams.getStartDate()), simpleDateFormat.parse(reportParams.getEndDate()), QUEUED, reportParams.getResponseType(), new Date());
        scheduledReportRepository.save(scheduledReport);
    }

    public static void scheduleCrons(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {

        BahmniReportsProperties bahmniReportsProperties=new BahmniReportsProperties();

        String triggerTime=bahmniReportsProperties.getCleanupJobTriggerTime();

        if(triggerTime==null) triggerTime="0 0 0 * * ?";

        JobKey jobKey=new JobKey("cleanjob","bahmnireports");

        if(schedulerFactoryBean.getScheduler().checkExists(jobKey)) {
           JobDetail existingJob= schedulerFactoryBean.getScheduler().getJobDetail(jobKey);
           String scheduleData= existingJob.getJobDataMap().get("scheduleData").toString();
           if(triggerTime.trim().equals(scheduleData.trim())) return;
            schedulerFactoryBean.getScheduler().interrupt(jobKey);
           schedulerFactoryBean.getScheduler().deleteJob(jobKey);
        }

        JobDetail cleanupJob= newJob(CleanReportsJob.class)
                .withIdentity(jobKey)
                .build();

        cleanupJob.getJobDataMap().put("scheduleData", triggerTime);

        Trigger cleanupTrigger;
        cleanupTrigger=newTrigger()
                .withSchedule(cronSchedule(triggerTime))
                .forJob(jobKey)
                .build();

        schedulerFactoryBean.getScheduler().scheduleJob(cleanupJob,cleanupTrigger);

        logger.info("Reports cleanup job scheduled for:"+triggerTime);
    }
}
