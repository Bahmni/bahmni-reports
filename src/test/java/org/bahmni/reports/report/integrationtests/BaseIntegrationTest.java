package org.bahmni.reports.report.integrationtests;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.web.MainReportController;
import org.bahmni.reports.wrapper.Report;
import org.bahmni.webclients.HttpClient;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
@TransactionConfiguration(defaultRollback = false)
public class BaseIntegrationTest extends BaseContextSensitiveTest {

    protected MockMvc mockMvc;

    private static boolean isBaseSetup;

    @Mock
    protected HttpClient httpClient;

    @Mock
    private JasperResponseConverter jasperResponseConverter;

    @Mock
    protected BahmniReportsProperties bahmniReportsProperties;

    @Mock
    private AllDatasources allDatasources;

    private BahmniReportsProperties dbProperties;

    @InjectMocks
    private MainReportController controller;


    @Before
    public void beforeBaseIntegrationTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(bahmniReportsProperties.getConfigFilePath()).thenReturn("src/test/resources/reports.json");
        when(bahmniReportsProperties.getOpenmrsRootUrl()).thenReturn(dbProperties.getOpenmrsRootUrl());
        when(bahmniReportsProperties.getOpenmrsServiceUser()).thenReturn(dbProperties.getOpenmrsServiceUser());
        when(bahmniReportsProperties.getOpenmrsConnectionTimeout()).thenReturn(dbProperties.getOpenmrsConnectionTimeout());
        when(bahmniReportsProperties.getOpenmrsReplyTimeout()).thenReturn(dbProperties.getOpenmrsReplyTimeout());
        when(allDatasources.getConnectionFromDatasource(any(BaseReportTemplate.class))).thenReturn(getDatabaseConnection());

        setUpTestData();
        Context.authenticate("admin", "test");
    }

    private void setUpTestData() throws Exception {
        deleteAllData();
        if (!Context.isSessionOpen()) {
            Context.openSession();
        }
        executeDataSet("datasets/initialTestDataSet.xml");
        executeDataSet("datasets/testDataSet.xml");
        getConnection().commit();
        Context.clearSession();
    }

    @Override
    public Boolean useInMemoryDatabase() {
        return false;
    }

    @Override
    public Properties getRuntimeProperties() {
        dbProperties = new BahmniReportsProperties("bahmni-reports-test.properties");
        Properties properties = new Properties();
        properties.put("connection.url", dbProperties.getOpenmrsUrl());
        properties.put("connection.username", dbProperties.getOpenmrsUser());
        properties.put("connection.password", dbProperties.getOpenmrsPassword());
        return properties;
    }

    protected Connection getDatabaseConnection() {
        try {
            Connection connection = DriverManager.getConnection(dbProperties.getOpenmrsUrl(),
                    dbProperties.getOpenmrsUser(), dbProperties.getOpenmrsPassword());
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Report fetchReport(String reportName, String startDate, String endDate) throws Exception {
        getConnection().commit();
        ArgumentCaptor<JasperReportBuilder> reportBuilderArgumentCaptor = ArgumentCaptor.forClass(JasperReportBuilder.class);
        doCallRealMethod().when(jasperResponseConverter).convert(any(String.class), reportBuilderArgumentCaptor.capture(),
                any(HttpServletResponse.class), any(String.class), any(String.class));
        String url = "/report?name=" + reportName + "&startDate=" + startDate + "&endDate=" + endDate + "&responseType=text/csv&paperSize=A3";
        ResultActions perform = mockMvc.perform(get(url));
        MvcResult mvcResult = perform.andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        perform.andExpect(status().isOk());
        return Report.getReport(result);
    }

    @Override
    public void deleteAllData() throws Exception {
        Context.clearSession();
        Connection connection = this.getConnection();
        this.turnOffDBConstraints(connection);
        String[] types = {"Table"};
        IDatabaseConnection dbUnitConn = this.setupDatabaseConnection(connection);
        ResultSet resultSet = connection.getMetaData().getTables((String) null, "PUBLIC", "%",types);
        DefaultDataSet dataset = new DefaultDataSet();

        while(resultSet.next()) {
            String tableName = resultSet.getString(3);
            dataset.addTable(new DefaultTable(tableName));
        }

        DatabaseOperation.DELETE_ALL.execute(dbUnitConn, dataset);
        this.turnOnDBConstraints(connection);
        connection.commit();
        this.updateSearchIndex();
        isBaseSetup = false;
    }

    private void turnOffDBConstraints(Connection connection) throws SQLException {
        String constraintsOffSql;
        if(this.useInMemoryDatabase().booleanValue()) {
            constraintsOffSql = "SET REFERENTIAL_INTEGRITY FALSE";
        } else {
            constraintsOffSql = "SET FOREIGN_KEY_CHECKS=0;";
        }

        PreparedStatement ps = connection.prepareStatement(constraintsOffSql);
        ps.execute();
        ps.close();
    }

    private IDatabaseConnection setupDatabaseConnection(Connection connection) throws DatabaseUnitException {
        DatabaseConnection dbUnitConn = new DatabaseConnection(connection);
        if(this.useInMemoryDatabase().booleanValue()) {
            DatabaseConfig config = dbUnitConn.getConfig();
            config.setProperty("http://www.dbunit.org/properties/datatypeFactory", new H2DataTypeFactory());
        }

        return dbUnitConn;
    }

    private void turnOnDBConstraints(Connection connection) throws SQLException {
        String constraintsOnSql;
        if(this.useInMemoryDatabase().booleanValue()) {
            constraintsOnSql = "SET REFERENTIAL_INTEGRITY TRUE";
        } else {
            constraintsOnSql = "SET FOREIGN_KEY_CHECKS=1;";
        }

        PreparedStatement ps = connection.prepareStatement(constraintsOnSql);
        ps.execute();
        ps.close();
    }
}