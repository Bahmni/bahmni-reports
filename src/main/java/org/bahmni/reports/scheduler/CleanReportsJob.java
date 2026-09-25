package org.bahmni.reports.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(CleanReportsJob.class);

    // Added for better testability
    private FileSystem fileSystem = new FileSystem() {};
    private ClockUtil clock = new ClockUtil();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            logger.info("Cleanup job triggered.");
            int days = bahmniReportsProperties.getDaysForHistoryReportsCleanup() != null ?
                    Integer.parseInt(bahmniReportsProperties.getDaysForHistoryReportsCleanup()) : 60;

            if (days < 0) {
                throw new IllegalArgumentException("Days for history reports cleanup cannot be negative");
            }

            Date cleanupDate = getCleanupDate(days);
            List<ScheduledReport> scheduledReportList = scheduledReportRepository.findByRequestDateTime(cleanupDate);

            for (ScheduledReport report : scheduledReportList) {
                if (report.getFileName() != null) {
                    File file = fileSystem.getFile(bahmniReportsProperties.getReportsSaveDirectory(), report.getFileName());
                    file.delete();
                    logger.info("Cleanup job removed report.{}", report.getFileName());
                }
                scheduledReportRepository.delete(report);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private Date getCleanupDate(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(clock.now());
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }

    // For testing purposes
    void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    // For testing purposes
    void setClock(ClockUtil clock) {
        this.clock = clock;
    }
}
