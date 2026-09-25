package org.bahmni.reports.scheduler;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.bahmni.reports.web.ReportParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.bahmni.reports.scheduler.ReportStatus.COMPLETED;
import static org.bahmni.reports.scheduler.ReportStatus.ERROR;
import static org.bahmni.reports.scheduler.ReportStatus.QUEUED;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;

public class ReportsSchedulerTest {
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
        MockitoAnnotations.openMocks(this);
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
        try (MockedStatic<JobBuilder> jobBuilderMock = Mockito.mockStatic(JobBuilder.class)) {
            JobDataMap jobDataMap = new JobDataMap();
            JobKey jobKey = new JobKey("jobName");
            JobDetail jobDetail = Mockito.mock(JobDetail.class);
            jobBuilderMock.when(() -> JobBuilder.newJob(ReportsJob.class)).thenReturn(jobBuilder);
            when(jobBuilder.build()).thenReturn(jobDetail);
            when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
            when(jobDetail.getKey()).thenReturn(jobKey);

            reportsScheduler.schedule(reportParams);

            assertThat((ReportParams) jobDataMap.get("reportParams"), is(reportParams));
        }
    }

    @Test
    public void shouldCreateCorrectTriggerForScheduling() throws Exception {
        try (MockedStatic<JobBuilder> jobBuilderMock = Mockito.mockStatic(JobBuilder.class);
             MockedStatic<TriggerBuilder> triggerBuilderMock = Mockito.mockStatic(TriggerBuilder.class)) {

            JobDataMap jobDataMap = new JobDataMap();
            JobKey jobKey = new JobKey("jobName");
            JobDetail jobDetail = Mockito.mock(JobDetail.class);
            jobBuilderMock.when(() -> JobBuilder.newJob(ReportsJob.class)).thenReturn(jobBuilder);
            when(jobBuilder.build()).thenReturn(jobDetail);
            when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
            when(jobDetail.getKey()).thenReturn(jobKey);

            TriggerBuilder triggerBuilder = Mockito.mock(TriggerBuilder.class);
            triggerBuilderMock.when(TriggerBuilder::newTrigger).thenReturn(triggerBuilder);
            when(triggerBuilder.startNow()).thenReturn(triggerBuilder);
            Trigger trigger = mock(Trigger.class);
            when(triggerBuilder.build()).thenReturn(trigger);

            reportsScheduler.schedule(reportParams);
        }
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
        // Use a test spy for file operations instead of constructor mocking
        ScheduledReport scheduledReport = new ScheduledReport();
        scheduledReport.setStatus(COMPLETED);
        scheduledReport.setFileName("fileName");
        when(scheduledReportRepository.findScheduledReportById("id")).thenReturn(scheduledReport);
        when(bahmniReportsProperties.getReportsSaveDirectory()).thenReturn("/home/bahmni/");

        // Create a spy for the file system operations
        File fileSpy = Mockito.spy(new File("dummy"));

        // Use reflection to insert our file spy
        ReflectionTestUtils.setField(reportsScheduler, "fileSystem", new FileSystem() {
            @Override
            public File getFile(String path) {
                return fileSpy;
            }
        });

        reportsScheduler.deleteScheduledReport("id");

        verify(fileSpy, times(1)).delete();
        verify(scheduledReportRepository, times(1)).delete(scheduledReport);
    }

    @Test
    public void shouldNotDeleteIfFileNameIsNull() throws Exception {
        ScheduledReport scheduledReport = new ScheduledReport();
        scheduledReport.setStatus(ERROR);
        when(scheduledReportRepository.findScheduledReportById("id")).thenReturn(scheduledReport);

        reportsScheduler.deleteScheduledReport("id");

        verify(bahmniReportsProperties, never()).getReportsSaveDirectory();
        verify(scheduledReportRepository, times(1)).delete(scheduledReport);
    }
}
