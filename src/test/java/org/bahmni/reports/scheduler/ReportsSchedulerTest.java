package org.bahmni.reports.scheduler;

import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.bahmni.reports.web.ReportParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.text.SimpleDateFormat;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.hibernate.*", "org.springframework.*"})
@PrepareForTest({JobBuilder.class, TriggerBuilder.class})
public class ReportsSchedulerTest {
    @Mock
    private Scheduler scheduler;

    @Mock
    private ScheduledReportRepository scheduledReportRepository;

    @Mock
    private JobBuilder jobBuilder;

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
        mockStatic(JobBuilder.class);
        JobDataMap jobDataMap = new JobDataMap();
        JobKey jobKey = new JobKey("jobName");
        JobDetail jobDetail = Mockito.mock(JobDetail.class);
        when(JobBuilder.newJob(ReportsJob.class)).thenReturn(jobBuilder);
        when(jobBuilder.build()).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(jobDetail.getKey()).thenReturn(jobKey);

        reportsScheduler.schedule(reportParams);

        assertThat((ReportParams) jobDataMap.get("reportParams"), is(reportParams));
    }

    @Test
    public void shouldCreateCorrectTriggerForScheduling() throws Exception {
        mockStatic(TriggerBuilder.class);
        mockStatic(JobBuilder.class);
        JobDataMap jobDataMap = new JobDataMap();
        JobKey jobKey = new JobKey("jobName");
        JobDetail jobDetail = Mockito.mock(JobDetail.class);
        when(JobBuilder.newJob(ReportsJob.class)).thenReturn(jobBuilder);
        when(jobBuilder.build()).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
        when(jobDetail.getKey()).thenReturn(jobKey);

        TriggerBuilder triggerBuilder = Mockito.mock(TriggerBuilder.class);
        PowerMockito.when(TriggerBuilder.newTrigger()).thenReturn(triggerBuilder);
        when(triggerBuilder.startNow()).thenReturn(triggerBuilder);
        Trigger trigger = mock(Trigger.class);
        when(triggerBuilder.build()).thenReturn(trigger);

        reportsScheduler.schedule(reportParams);
    }
}