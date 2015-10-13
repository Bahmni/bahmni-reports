package org.bahmni.reports.org.bahmni.reports.report;

import com.opencsv.CSVReader;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.web.MainReportController;
import org.bahmni.webclients.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletResponse;
import java.io.StringReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:TestApplicationContext.xml"}, inheritLocations = true)
public class SomeIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private HttpClient httpClient;

    @Mock
    private JasperResponseConverter jasperResponseConverter;

    @Mock
    private BahmniReportsProperties bahmniReportsProperties;

    @Mock
    private AllDatasources allDatasources;

    @InjectMocks
    MainReportController controller;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        String json = "{\"datatype\":{\"display\":\"coded\"}}";//TODO: update it with correct json
        when(httpClient.get(any(URI.class))).thenReturn(json);
        when(bahmniReportsProperties.getConfigFilePath()).thenReturn("src/test/resources/reports.json");
        when(allDatasources.getConnectionFromDatasource(any(BaseReportTemplate.class))).thenReturn(getConnection());
    }

    protected CSVReader run(String url) throws Exception {
        ArgumentCaptor<JasperReportBuilder> reportBuilderArgumentCaptor = ArgumentCaptor.forClass(JasperReportBuilder.class);
        doCallRealMethod().when(jasperResponseConverter).convert(any(String.class), reportBuilderArgumentCaptor.capture(),
                any(HttpServletResponse.class), any(String.class), any(String.class));
        ResultActions perform = mockMvc.perform(get(url));
        MvcResult mvcResult = perform.andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        perform.andExpect(status().isOk());
        return new CSVReader(new StringReader(result), ',');
    }

    @Test
    public void shouldRetrieveOrderFulfillmentReport() throws Exception {
        CSVReader csvReader = run("/report?name=Order Fulfillment Report&startDate=2010-02-03&endDate=2015-10-12&responseType=text/csv&paperSize=A3");
        String[] row;
        row = csvReader.readNext();
        assertEquals(9, row.length);
        assertEquals("Order Fulfillment Report", row[0]);
    }

    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://172.17.0.13:3306/openmrs?allowMultiQueries=true",
                    "openmrs-user", "password");
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
