package org.bahmni.reports.report;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.web.MainReportController;
import org.bahmni.reports.wrapper.Report;
import org.bahmni.webclients.HttpClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.SkipBaseSetupAnnotationExecutionListener;
import org.openmrs.test.StartModuleExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:TestApplicationContext.xml",}, inheritLocations = true)
public class BaseIntegrationTest extends BaseContextSensitiveTest {

    protected MockMvc mockMvc;

    @Mock
    private HttpClient httpClient;

    @Mock
    private JasperResponseConverter jasperResponseConverter;

    @Mock
    private BahmniReportsProperties bahmniReportsProperties;

    @Mock
    private AllDatasources allDatasources;

    @Autowired
    private BahmniReportsProperties dbProperties;

    @InjectMocks
    MainReportController controller;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        String json = "{\"datatype\":{\"display\":\"coded\"}}";//TODO: update it with correct json
        when(httpClient.get(any(URI.class))).thenReturn(json);
        when(bahmniReportsProperties.getConfigFilePath()).thenReturn("src/test/resources/reports.json");
        when(allDatasources.getConnectionFromDatasource(any(BaseReportTemplate.class))).thenReturn(getDatabaseConnection());
    }

    protected Connection getDatabaseConnection() {
        try {
            Connection connection = DriverManager.getConnection(dbProperties.getOpenmrsUrl(),
                    dbProperties.getOpenmrsUser(), dbProperties.getOpenmrsPassword());
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Report fetchReport(String reportName, String startDate, String endDate, String responseType, String paperSize) throws Exception {
        ArgumentCaptor<JasperReportBuilder> reportBuilderArgumentCaptor = ArgumentCaptor.forClass(JasperReportBuilder.class);
        doCallRealMethod().when(jasperResponseConverter).convert(any(String.class), reportBuilderArgumentCaptor.capture(),
                any(HttpServletResponse.class), any(String.class), any(String.class));
        String url = "/report?name=" + reportName + "&startDate=" + startDate + "&endDate=" + endDate + "&responseType=" + responseType + "&paperSize=" + paperSize;
        ResultActions perform = mockMvc.perform(get(url));
        MvcResult mvcResult = perform.andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        perform.andExpect(status().isOk());
        return Report.getReport(result);
    }
}