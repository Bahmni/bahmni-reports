package org.bahmni.reports.report.integrationtests;

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
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
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
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:TestApplicationContext.xml"}, inheritLocations = true)
@Transactional
@TransactionConfiguration(defaultRollback = false)
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

    private Connection connection;

    @Before
    public void beforeBaseIntegrationTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        String json = "{\"datatype\":{\"display\":\"coded\"}}";//TODO: update it with correct json
        when(httpClient.get(any(URI.class))).thenReturn(json);
        when(bahmniReportsProperties.getConfigFilePath()).thenReturn("src/test/resources/reports.json");
        when(allDatasources.getConnectionFromDatasource(any(BaseReportTemplate.class))).thenReturn(getDatabaseConnection());
        setUpTestData();
    }

    private void setUpTestData() throws Exception {
        deleteAllData();
        if (!Context.isSessionOpen()) {
            Context.openSession();
        }
        executeDataSet("initialTestDataSet.xml");
        executeDataSet("testDataSet.xml");
        getConnection().commit();
        Context.clearSession();
    }

    @Override
    public Boolean useInMemoryDatabase() {
        return Boolean.valueOf(false);
    }

    @Override
    public Properties getRuntimeProperties() {
        BahmniReportsProperties dbProperties = new BahmniReportsProperties();
        Properties properties = new Properties();
        properties.put("connection.url", dbProperties.getOpenmrsUrl());
        properties.put("connection.username", dbProperties.getOpenmrsUser());
        properties.put("connection.password", dbProperties.getOpenmrsPassword());
        return properties;
    }

    protected Connection getDatabaseConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbProperties.getOpenmrsUrl(),
                        dbProperties.getOpenmrsUser(), dbProperties.getOpenmrsPassword());
                connection.setAutoCommit(true);
            }
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