package org.bahmni.reports.scheduler;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.persistence.ScheduledReport;
import org.bahmni.reports.persistence.ScheduledReportRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CleanReportsJobTest {

    @Rule
    public TemporaryFolder reportsDirectory = new TemporaryFolder();

    @Mock
    private BahmniReportsProperties bahmniReportsProperties;

    @Mock
    private ScheduledReportRepository scheduledReportRepository;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private CleanReportsJob cleanReportsJob;

    @Test
    public void shouldNotDeleteFileWhenTheDBReportFileNameIsNull() throws Exception {
        when(bahmniReportsProperties.getDaysForHistoryReportsCleanup()).thenReturn("60");
        List<ScheduledReport> scheduledReports = new ArrayList<>();
        scheduledReports.add(scheduledReport("1", "testReport", null));
        when(scheduledReportRepository.findByRequestDateTime(any(Date.class))).thenReturn(scheduledReports);

        cleanReportsJob.execute(jobExecutionContext);

        // Never reading the save directory is the whole assertion: it is the only way the job
        // could reach a file, so not reading it proves no delete was attempted.
        verify(bahmniReportsProperties, never()).getReportsSaveDirectory();
        verify(scheduledReportRepository).delete(scheduledReports.get(0));
    }

    @Test
    public void testCleanupJobDeletesFileAndDbReportWhenTriggers() throws Exception {
        when(bahmniReportsProperties.getDaysForHistoryReportsCleanup()).thenReturn("60");
        givenReportsSaveDirectory();
        File reportFile = reportsDirectory.newFile("testFileName");
        List<ScheduledReport> scheduledReports = new ArrayList<>();
        scheduledReports.add(scheduledReport("1", "testReport", "testFileName"));
        when(scheduledReportRepository.findByRequestDateTime(any(Date.class))).thenReturn(scheduledReports);

        cleanReportsJob.execute(jobExecutionContext);

        assertFalse(reportFile.exists());
        verify(scheduledReportRepository).delete(scheduledReports.get(0));
    }

    @Test
    public void testCleanupJobDeletesMultipleFileAndDbReportWhenTriggers() throws Exception {
        when(bahmniReportsProperties.getDaysForHistoryReportsCleanup()).thenReturn(null);
        givenReportsSaveDirectory();
        File firstReportFile = reportsDirectory.newFile("testFileName1");
        File secondReportFile = reportsDirectory.newFile("testFileName2");
        List<ScheduledReport> scheduledReports = new ArrayList<>();
        scheduledReports.add(scheduledReport("1", "testReport1", "testFileName1"));
        scheduledReports.add(scheduledReport("2", "testReport2", "testFileName2"));
        when(scheduledReportRepository.findByRequestDateTime(any(Date.class))).thenReturn(scheduledReports);

        cleanReportsJob.execute(jobExecutionContext);

        assertFalse(firstReportFile.exists());
        assertFalse(secondReportFile.exists());
        verify(scheduledReportRepository).delete(scheduledReports.get(0));
        verify(scheduledReportRepository).delete(scheduledReports.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionsWhenDaysAreNegetive() throws JobExecutionException {
        when(bahmniReportsProperties.getDaysForHistoryReportsCleanup()).thenReturn("-60");
        cleanReportsJob.execute(jobExecutionContext);
    }

    @Test
    public void shouldReturnProperCleupDate() throws Exception {
        when(bahmniReportsProperties.getDaysForHistoryReportsCleanup()).thenReturn("10");

        Date beforeExecution = new Date();
        cleanReportsJob.execute(jobExecutionContext);
        Date afterExecution = new Date();

        ArgumentCaptor<Date> cleanupDate = ArgumentCaptor.forClass(Date.class);
        verify(scheduledReportRepository).findByRequestDateTime(cleanupDate.capture());
        // The job cleans up reports older than "now minus ten days". Bounding the captured date by
        // the same calendar arithmetic applied to the instants either side of the call pins the ten
        // days exactly, without needing to mock the clock.
        assertFalse(cleanupDate.getValue().before(daysBefore(beforeExecution, 10)));
        assertFalse(cleanupDate.getValue().after(daysBefore(afterExecution, 10)));
    }

    private void givenReportsSaveDirectory() {
        when(bahmniReportsProperties.getReportsSaveDirectory()).thenReturn(reportsDirectory.getRoot().getAbsolutePath());
    }

    private ScheduledReport scheduledReport(String id, String name, String fileName) {
        return new ScheduledReport(id, name, "super", fileName, new Date(), new Date(), "test", "test", new Date());
    }

    private Date daysBefore(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -days);
        return calendar.getTime();
    }
}
