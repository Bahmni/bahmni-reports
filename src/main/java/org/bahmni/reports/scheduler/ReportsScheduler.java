package org.bahmni.reports.scheduler;

import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.bahmni.reports.web.ReportParams;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class ReportsScheduler {
    public static final String SCHEDULED = "Scheduled";

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduledReportRepository scheduledReportRepository;

    public ReportsScheduler() {
    }

    public void schedule(ReportParams reportParams) throws ParseException, SchedulerException, UnsupportedEncodingException {
        JobDetail job = newJob(ReportsJob.class).build();
        job.getJobDataMap().put("reportParams", reportParams);

        Trigger trigger = newTrigger().startNow().build();

        scheduler.scheduleJob(job, trigger);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy-mm-dd");
        ScheduledReport scheduledReport = new ScheduledReport(job.getKey().getName(), reportParams.getName(), reportParams.getUserName(), null, simpleDateFormat.parse(reportParams.getStartDate()), simpleDateFormat.parse(reportParams.getEndDate()), SCHEDULED, reportParams.getResponseType(), new Date());
        scheduledReportRepository.save(scheduledReport);
    }

}
