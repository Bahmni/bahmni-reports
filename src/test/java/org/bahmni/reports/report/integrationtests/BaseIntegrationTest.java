package org.bahmni.reports.report.integrationtests;

import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.filter.JasperResponseConverter;
import org.bahmni.reports.model.AllDatasources;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.web.MainReportController;
import org.bahmni.reports.web.ReportParams;
import org.bahmni.reports.web.security.OpenMRSAuthenticator;
import org.bahmni.reports.web.security.Privileges;
import org.bahmni.reports.web.security.Privilege;
import org.bahmni.reports.wrapper.CsvReport;
import org.bahmni.webclients.HttpClient;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
@SkipBaseSetup
public class BaseIntegrationTest extends BaseContextSensitiveTest {

    protected MockMvc mockMvc;

    @Mock
    private OpenMRSAuthenticator openMRSAuthenticator;

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

    private String configFileUrl = "src/test/resources/config/reports.json";

    public BaseIntegrationTest(String configFilePath) {
        this.configFileUrl = configFilePath;
    }

    public BaseIntegrationTest() {
    }

    @Before
    public void beforeBaseIntegrationTest() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(bahmniReportsProperties.getConfigFileUrl()).thenReturn(configFileUrl);
        when(bahmniReportsProperties.getOpenmrsRootUrl()).thenReturn(dbProperties.getOpenmrsRootUrl());
        when(bahmniReportsProperties.getOpenmrsServiceUser()).thenReturn(dbProperties.getOpenmrsServiceUser());
        when(bahmniReportsProperties.getOpenmrsServicePassword()).thenReturn(dbProperties.getOpenmrsServicePassword());
        when(bahmniReportsProperties.getOpenmrsConnectionTimeout()).thenReturn(dbProperties.getOpenmrsConnectionTimeout());
        when(bahmniReportsProperties.getOpenmrsReplyTimeout()).thenReturn(dbProperties.getOpenmrsReplyTimeout());
        when(bahmniReportsProperties.getMacroTemplatesTempDirectory()).thenReturn("/tmp");
        /*
         * A fresh connection per call, not one shared instance. Report rendering closes the
         * connection it was handed, so thenReturn(...) -- which hands every caller the same
         * object -- only works while a test runs exactly one report. The second report in the
         * same test method gets a closed connection and fails with "No operations allowed
         * after connection closed", or with a JRException wrapping it. A real DataSource hands
         * out a connection per call, so this also matches production more closely.
         */
        when(allDatasources.getConnectionFromDatasource(any(BaseReportTemplate.class)))
                .thenAnswer(invocation -> getDatabaseConnection());

        String fileData=FileUtils.readFileToString(new File(configFileUrl));
        when(httpClient.get(any(URI.class))).thenReturn(fileData);
        setUpTestData();
        Context.authenticate("admin", "test");

        Privileges privileges = new Privileges();
        Privilege privilege = new Privilege();
        String privilegeName = "privilege";
        FieldUtils.writeField(privilege, "name", privilegeName, true);
        privileges.add(privilege);
        when(openMRSAuthenticator.callOpenMRS("")).thenReturn(ResponseEntity.ok(privileges));
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

    /*
     * BaseContextSensitiveTest.deleteAllData() enumerates tables via
     * DatabaseMetaData.getTables(catalog, "PUBLIC", "%", null). At openmrs-api 2.5.7 the
     * catalog argument was null, which the root user create_configuration.sh connects as
     * can see every database on the server (including the sibling bahmni_reports_it, and
     * performance_schema on a plain MySQL install), turning that into "every database
     * visible to this connection" instead of just this one. From 2.6.0 the catalog argument
     * is System.getProperty("databaseName") instead of null (see getRuntimeProperties()
     * below, which sets it to match this project's actual database), so that half of the
     * problem no longer applies -- but the table-type filter is still null in both versions,
     * which includes VIEW. This schema has non-updatable joined views (e.g.
     * diagnosis_concept_view) that the reports query directly, so they must stay in the
     * schema but must never be handed to DELETE_ALL, and tables from the sibling
     * bahmni_reports_it database (e.g. scheduled_report) must not appear at all.
     * deleteAllData() is not overridable, so the fix intercepts at getConnection(), which it
     * does call virtually, and narrows the table-type filter for that one unscoped lookup,
     * falling back to the connection's own catalog only when the caller passed none.
     */
    @Override
    public Connection getConnection() {
        final Connection realConnection = super.getConnection();
        return (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("getMetaData".equals(method.getName())) {
                        return wrapMetaDataExcludingViews(realConnection.getMetaData());
                    }
                    return invokeReal(method, realConnection, args);
                });
    }

    /*
     * Proxy.newProxyInstance requires the handler to throw only what the invoked interface
     * method itself declares (plus unchecked exceptions); anything else is wrapped in an
     * UndeclaredThrowableException by the JDK. Method.invoke's own checked exception,
     * InvocationTargetException, is never one of those declared types, so calling it
     * directly here would turn every real SQLException from the proxied Connection or
     * DatabaseMetaData into an UndeclaredThrowableException instead. Unwrap it back to the
     * real cause before it leaves the handler.
     */
    private static Object invokeReal(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /*
     * BaseContextSensitiveTest.setupDatabaseConnection() never registers a MySQL-aware
     * DatabaseConfig.PROPERTY_DATATYPE_FACTORY for the real-database path (only the
     * in-memory H2 path gets one), so DBUnit falls back to DefaultDataTypeFactory and warns
     * on every table it inspects that "MySQL" isn't in its list of recognised products.
     * Harmless, but it drowns real signal in test output across hundreds of tables.
     */
    @Override
    protected IDatabaseConnection setupDatabaseConnection(Connection connection) throws DatabaseUnitException {
        IDatabaseConnection dbUnitConnection = super.setupDatabaseConnection(connection);
        dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
        return dbUnitConnection;
    }

    /*
     * deleteAllData() always finishes with a Lucene reindex over ConceptName, Drug,
     * PersonName, PersonAttribute and PatientIdentifier. It is suppressed because none of
     * this app's reports go through OpenMRS's search API: they run raw SQL against the
     * tables directly, so the reindex verifies nothing here and costs time in every
     * test's @Before.
     *
     * It originally existed for a different reason, now dead: the old 2.1.x-era fixture
     * lacked drug.dose_limit_units, which Drug's Hibernate mapping reads, so the reindex
     * errored in every test. The 2.8.9 capture has that column. Keep the override anyway,
     * for the reason above, rather than removing it because that reason no longer applies.
     */
    @Override
    public void updateSearchIndex() {
    }

    private DatabaseMetaData wrapMetaDataExcludingViews(final DatabaseMetaData realMetaData) {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{DatabaseMetaData.class},
                (proxy, method, args) -> {
                    boolean isViewInclusiveTableTypeScan = "getTables".equals(method.getName())
                            && args != null && args.length == 4 && includesViews(args[3]);
                    if (isViewInclusiveTableTypeScan) {
                        Object catalog = args[0] != null ? args[0] : realMetaData.getConnection().getCatalog();
                        String[] withoutViews = args[3] == null
                                ? new String[]{"TABLE"}
                                : Arrays.stream((String[]) args[3])
                                        .filter(type -> !"VIEW".equalsIgnoreCase(type))
                                        .toArray(String[]::new);
                        Object[] scoped = {catalog, args[1], args[2], withoutViews};
                        return invokeReal(method, realMetaData, scoped);
                    }
                    return invokeReal(method, realMetaData, args);
                });
    }

    /*
     * A null type filter means "every type", which includes VIEW; an explicit filter that
     * still lists VIEW alongside other types has the same effect. Either shape must be
     * narrowed above, not just the null case deleteAllData() happens to use today.
     */
    private static boolean includesViews(Object typeFilterArg) {
        if (typeFilterArg == null) {
            return true;
        }
        if (!(typeFilterArg instanceof String[])) {
            return false;
        }
        for (String type : (String[]) typeFilterArg) {
            if ("VIEW".equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    /*
     * From openmrs-api 2.6.0 onward, BaseContextSensitiveTest.deleteAllData() and the
     * OpenmrsMetadataHandler DBUnit uses for every real-database test both scope their
     * DatabaseMetaData.getTables() catalog lookup to a fixed database name -- respectively
     * System.getProperty("databaseName") and the mutable OpenmrsConstants.DATABASE_NAME --
     * instead of the connection's own catalog. Both default to "openmrs", the name
     * Containers.ensureMySQLRunning() gives its own throwaway Testcontainers database. This
     * project's actual schema lives in whatever database connection.url points to (see
     * bahmni-reports-test.properties), so both must be set to match or every table lookup
     * silently scopes to a catalog that doesn't exist, surfacing as NoSuchTableException.
     */
    @Override
    public Properties getRuntimeProperties() {
        dbProperties = new BahmniReportsProperties("bahmni-reports-test.properties");
        assertTuningParametersPresent(dbProperties.getOpenmrsUrl());
        Properties properties = new Properties();
        properties.put("connection.url", dbProperties.getOpenmrsUrl());
        properties.put("connection.username", dbProperties.getOpenmrsUser());
        properties.put("connection.password", dbProperties.getOpenmrsPassword());
        String urlWithoutQuery = dbProperties.getOpenmrsUrl().split("\\?", 2)[0];
        String databaseName = urlWithoutQuery.substring(urlWithoutQuery.lastIndexOf('/') + 1);
        OpenmrsConstants.DATABASE_NAME = databaseName;
        System.setProperty("databaseName", databaseName);
        return properties;
    }

    /*
     * The properties file is generated by scripts/create_configuration.sh, but the exec-plugin
     * step that runs it is gated behind skipConfig, which defaults to true. A developer who
     * generated the file before these parameters existed keeps the old URL, and the symptom is
     * not an error: without optimizer_search_depth, observationFormReport.sql's 22-table join
     * spends 370+ seconds in MySQL 8 join-order planning, so the suite appears to hang with
     * nothing in any log. Fail immediately, naming the fix, rather than letting that happen.
     */
    private void assertTuningParametersPresent(String openmrsUrl) {
        for (String parameter : new String[]{"optimizer_search_depth", "sql_mode"}) {
            if (openmrsUrl == null || !openmrsUrl.contains(parameter)) {
                throw new IllegalStateException("openmrs.url in bahmni-reports-test.properties is"
                        + " missing the '" + parameter + "' session variable. Regenerate the file"
                        + " with 'sh scripts/create_configuration.sh' rather than editing it."
                        + " Current value: " + openmrsUrl);
            }
        }
    }

    /*
     * Throws rather than returning null. This is now on the per-render path (see the
     * thenAnswer stub above), so a transient connection failure used to surface as an NPE
     * inside whichever report happened to run next, blaming the wrong thing entirely.
     */
    protected Connection getDatabaseConnection() {
        try {
            return DriverManager.getConnection(dbProperties.getOpenmrsUrl(),
                    dbProperties.getOpenmrsUser(), dbProperties.getOpenmrsPassword());
        } catch (SQLException e) {
            throw new IllegalStateException("could not open a connection to "
                    + dbProperties.getOpenmrsUrl() + ". Is the MySQL container running?", e);
        }
    }

    protected CsvReport fetchCsvReport(String reportName, String startDate, String endDate) throws Exception {
        return fetchCsvReport(reportName, startDate, endDate, false);
    }

    protected CsvReport fetchCsvReport(String reportName, String startDate, String endDate, boolean ignoreStatusCheck) throws Exception {
        MvcResult mvcResult = fetchMvcResult(reportName, startDate, endDate, "text/csv", ignoreStatusCheck);
        String enc = "utf-8";
        MockHttpServletResponse response = mvcResult.getResponse();
        response.setCharacterEncoding(enc);
        String result = response.getContentAsString();
        return CsvReport.getReport(result, response.getErrorMessage());
    }

    protected XSSFWorkbook fetchXlsReport(String reportName, String startDate, String endDate) throws Exception {
        MvcResult mvcResult = fetchMvcResult(reportName, startDate, endDate, "application/vnd.ms-excel", false);
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(mvcResult.getResponse().getContentAsByteArray()));
        return workbook;
    }

    protected MvcResult fetchMvcResult(String reportName, String startDate, String endDate, String responseType, boolean ignoreStatusCheck) throws Exception {
        getConnection().commit();
        doCallRealMethod().when(jasperResponseConverter).applyReportTemplates(any(List.class),
                any(String.class));
        doCallRealMethod().when(jasperResponseConverter).applyHttpHeaders(
                any(String.class), any(HttpServletResponse.class), anyString());
        doCallRealMethod().when(jasperResponseConverter).convertToResponseType(any(ReportParams.class), anyString(), any(OutputStream.class), any(JasperConcatenatedReportBuilder.class));
        String url = "/report?name=" + reportName + "&startDate=" + startDate + "&endDate=" + endDate + "&responseType=" + responseType + "&paperSize=A3";
        ResultActions perform = mockMvc.perform(get(url));
        final MvcResult mvcResult = perform.andReturn();
        if (!ignoreStatusCheck)
            perform.andExpect(status().isOk());
        return mvcResult;
    }

    protected JasperReportBuilder fetchReportBuilder(String reportName, String startDate, String endDate) throws Exception {
        getConnection().commit();
        ArgumentCaptor<List> reportBuilderArgumentCaptor = ArgumentCaptor.forClass(List.class);
        doCallRealMethod().when(jasperResponseConverter).applyReportTemplates(reportBuilderArgumentCaptor.capture(),
                any(String.class));
        doCallRealMethod().when(jasperResponseConverter).applyHttpHeaders(
                any(String.class), any(HttpServletResponse.class), anyString());
        doCallRealMethod().when(jasperResponseConverter).convertToResponseType(any(ReportParams.class), anyString(), any(OutputStream.class), any(JasperConcatenatedReportBuilder.class));
        String url = "/report?name=" + reportName + "&startDate=" + startDate + "&endDate=" + endDate + "&responseType=text/csv&paperSize=A3";
        ResultActions perform = mockMvc.perform(get(url));
        perform.andReturn();
        perform.andExpect(status().isOk());
        List<JasperReportBuilder> value = reportBuilderArgumentCaptor.getValue();
        return value.get(0);
    }
}
