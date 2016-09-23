package org.bahmni.reports.scheduler;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CleanReportsJob implements Job {

    @Autowired
    private BahmniReportsProperties bahmniReportsProperties;

    @Autowired
    private ScheduledReportRepository scheduledReportRepository;

    private static final Logger logger = Logger.getLogger(CleanReportsJob.class);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            logger.info("Cleanup job triggered.");
            int days = bahmniReportsProperties.getDaysForHistoryReportsCleanup() != null ?
                    Integer.parseInt(bahmniReportsProperties.getDaysForHistoryReportsCleanup()) : 60;
            Date cleanupDate = getCleanupDate(days);
            List<ScheduledReport> scheduledReportList = scheduledReportRepository.findByRequestDateTime(cleanupDate);
            for (int i = 0; i < scheduledReportList.size(); i++) {
                if (scheduledReportList.get(i).getFileName() != null) {
                    File file = new File(bahmniReportsProperties.getReportsSaveDirectory(), scheduledReportList.get(i).getFileName());
                    file.delete();
                    logger.info("Cleanup job removed report."+scheduledReportList.get(i).getFileName());
                }
                scheduledReportRepository.delete(scheduledReportList.get(i));
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private Date getCleanupDate(int days) {
        isValid(days);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -days);
        return cal.getTime();
    }

    private void isValid(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
    }

}
