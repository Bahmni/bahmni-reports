package org.bahmni.reports.scheduler;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.bahmni.reports.web.ReportParams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.io.File;

import static org.bahmni.reports.scheduler.ReportStatus.COMPLETED;
import static org.bahmni.reports.scheduler.ReportStatus.ERROR;
import static org.bahmni.reports.scheduler.ReportStatus.QUEUED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReportsSchedulerTest {

    @Rule
    public TemporaryFolder reportsDirectory = new TemporaryFolder();

    @Mock
    private Scheduler scheduler;

    @Mock
    private ScheduledReportRepository scheduledReportRepository;

    @Mock
    private JobBuilder jobBuilder;

    @Mock
    private BahmniReportsProperties bahmniReportsProperties;

    @InjectMocks
    private ReportsScheduler reportsScheduler;

    private ReportParams reportParams;

    @Before
    public void setUp() {
        this.reportParams = new ReportParams();
        this.reportParams.setStartDate("2016-09-16");
        this.reportParams.setEndDate("2016-09-17");
        this.reportParams.setAppName("AppName");
        this.reportParams.setPaperSize("A4");
        this.reportParams.setName("Sample Report");
        this.reportParams.setUserName("UserName");
    }

    @Test
    public void shouldCreateCorrectJobForScheduling() throws Exception {
        JobDataMap jobDataMap = new JobDataMap();
        JobKey jobKey = new JobKey("jobName");
        JobDetail jobDetail = Mockito.mock(JobDetail.class);

        try (MockedStatic<JobBuilder> jobBuilderStatic = mockStatic(JobBuilder.class)) {
            jobBuilderStatic.when(() -> JobBuilder.newJob(ReportsJob.class)).thenReturn(jobBuilder);
            when(jobBuilder.build()).thenReturn(jobDetail);
            when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
            when(jobDetail.getKey()).thenReturn(jobKey);

            reportsScheduler.schedule(reportParams);
        }

        assertThat((ReportParams) jobDataMap.get("reportParams"), is(reportParams));
    }

    @Test
    public void shouldCreateCorrectTriggerForScheduling() throws Exception {
        JobDataMap jobDataMap = new JobDataMap();
        JobKey jobKey = new JobKey("jobName");
        JobDetail jobDetail = Mockito.mock(JobDetail.class);
        TriggerBuilder triggerBuilder = Mockito.mock(TriggerBuilder.class);
        Trigger trigger = mock(Trigger.class);

        try (MockedStatic<JobBuilder> jobBuilderStatic = mockStatic(JobBuilder.class);
             MockedStatic<TriggerBuilder> triggerBuilderStatic = mockStatic(TriggerBuilder.class)) {
            jobBuilderStatic.when(() -> JobBuilder.newJob(ReportsJob.class)).thenReturn(jobBuilder);
            when(jobBuilder.build()).thenReturn(jobDetail);
            when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
            when(jobDetail.getKey()).thenReturn(jobKey);

            triggerBuilderStatic.when(TriggerBuilder::newTrigger).thenReturn(triggerBuilder);
            when(triggerBuilder.startNow()).thenReturn(triggerBuilder);
            when(triggerBuilder.build()).thenReturn(trigger);

            reportsScheduler.schedule(reportParams);
        }

        verify(scheduler, times(1)).scheduleJob(jobDetail, trigger);
    }

    @Test
    public void shouldDeleteQueuedReportsJob() throws Exception {
        ScheduledReport scheduledReport = new ScheduledReport();
        scheduledReport.setFileName("reportName");
        scheduledReport.setStatus(QUEUED);
        when(scheduledReportRepository.findScheduledReportById("id")).thenReturn(scheduledReport);

        reportsScheduler.deleteScheduledReport("id");

        verify(scheduler,times(1)).deleteJob(JobKey.jobKey("id"));
        verify(scheduledReportRepository, times(1)).delete(scheduledReport);
    }

    @Test
    public void shouldDeleteTheFileOfCompletedJob() throws Exception {
        ScheduledReport scheduledReport = new ScheduledReport();
        scheduledReport.setStatus(COMPLETED);
        scheduledReport.setFileName("fileName");
        when(scheduledReportRepository.findScheduledReportById("id")).thenReturn(scheduledReport);
        when(bahmniReportsProperties.getReportsSaveDirectory()).thenReturn(reportsDirectory.getRoot().getAbsolutePath());
        File reportFile = reportsDirectory.newFile("fileName");

        reportsScheduler.deleteScheduledReport("id");

        assertFalse(reportFile.exists());
        verify(scheduledReportRepository, times(1)).delete(scheduledReport);
    }

    @Test
    public void shouldNotDeleteIfFileNameIsNull() throws  Exception {
        ScheduledReport scheduledReport = new ScheduledReport();
        scheduledReport.setStatus(ERROR);
        when(scheduledReportRepository.findScheduledReportById("id")).thenReturn(scheduledReport);

        reportsScheduler.deleteScheduledReport("id");

        // Never reading the save directory is the whole assertion: getFilePath is the only route
        // to a File, so not reading it proves no delete was attempted.
        verify(bahmniReportsProperties, never()).getReportsSaveDirectory();
        verify(scheduledReportRepository, times(1)).delete(scheduledReport);
    }
}
