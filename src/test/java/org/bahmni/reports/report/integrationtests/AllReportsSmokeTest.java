package org.bahmni.reports.report.integrationtests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.bahmni.reports.model.ConceptDetails;
import org.bahmni.reports.model.ConceptName;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.wrapper.CsvReport;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getConceptNamesParameter;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/*
 * Runs every report type this service can serve against the OpenMRS datasource, end to end
 * through the real controller, and checks each one executes without a SQL error.
 *
 * Why this exists: for a long time none of the files under src/main/resources/sql/ were
 * executed by the suite at all. The schema fixture in src/test/resources/sql/ could therefore
 * drift from the shape a real OpenMRS deployment has, and every test would still pass. That
 * fixture is now captured from a real openmrs-core instance rather than hand-maintained, and
 * this class is what keeps the report SQL honest against it on every run.
 *
 * The reports are driven by name out of allReportsSmokeConfig.json rather than by reading the
 * .sql files directly, because those files are StringTemplate templates with #token#
 * placeholders that only the DAO/template layer can render -- executing a raw file would test
 * nothing that runs in production.
 */
public class AllReportsSmokeTest extends BaseIntegrationTest {

    private static final String CONFIG_PATH = "src/test/resources/config/allReportsSmokeConfig.json";

    /* Wide enough that no report is empty merely because of its date filter. */
    private static final String START_DATE = "2000-01-01";
    private static final String END_DATE = "2030-12-31";

    /* The observation template and form name seeded by cannedReportsDataSet.xml. */
    private static final String TEMPLATE_NAME = "Canned Vitals";

    /*
     * Report types deliberately outside this smoke test, each for a reason that is about the
     * harness rather than about the report. Keep this list honest: it is subtracted from the
     * completeness check below, so anything added here stops being covered.
     */
    private static final Set<String> NOT_SMOKE_TESTABLE = new LinkedHashSet<>(Arrays.asList(
            /* Not the OpenMRS datasource. These query OpenELIS or Odoo (Postgres) or
             * BahmniMart, none of which the test harness stands up at all. */
            "TestCount", "ElisGeneric", "ERPGeneric", "MartGeneric", "MartConcatenated",
            /* Call an external SNOMED terminology server before running their SQL. The shared
             * httpClient mock in BaseIntegrationTest answers every URI with reports.json, so
             * these need their own stub rather than a config entry. */
            "fhirTSLookupDiagnosisCount", "fhirTSLookupDiagnosisLine",
            /* Composite types: they name other reports rather than carrying SQL of their own,
             * and both already have dedicated tests (AggregationReportTest,
             * OpenmrsConcatenatedReportTest) with their own multi-entry configs. */
            "aggregation", "concatenated"));

    public AllReportsSmokeTest() {
        super(CONFIG_PATH);
    }

    @Before
    public void setUpSmokeData() throws Exception {
        executeDataSet("datasets/cannedReportsDataSet.xml");
        getConnection().commit();
        stubOpenmrsLookups();
    }

    /*
     * Several report types resolve their columns by calling OpenMRS over HTTP before they run
     * any SQL: the obs-template reports expand a template into its leaf concepts, and the two
     * form reports fetch the published form list and then each form's definition. The shared
     * httpClient mock in BaseIntegrationTest answers every URI with reports.json, which
     * deserialises into the wrong shape, so each endpoint needs its own answer.
     *
     * The concept-name URLs below go through getConceptNamesParameter, the same helper the
     * production code uses. The two form URLs do not: they duplicate
     * ObservationFormReportTemplate.java:103 and :125 as literal strings, so a change there
     * will not be reflected here. That drift is loud rather than silent (the catch-all mock
     * returns reports.json and Jackson throws), but it is drift.
     */
    private void stubOpenmrsLookups() throws Exception {
        String root = bahmniReportsProperties.getOpenmrsRootUrl();
        ObjectMapper mapper = new ObjectMapper();

        ConceptDetails height = new ConceptDetails();
        height.setName("Canned Height");
        height.setFullName("Canned Height");
        height.setAttributes(new HashMap<>());
        ConceptDetails weight = new ConceptDetails();
        weight.setName("Canned Weight");
        weight.setFullName("Canned Weight");
        weight.setAttributes(new HashMap<>());
        List<ConceptDetails> leafConcepts = Arrays.asList(height, weight);

        URI leafConceptsUri = new URI(root + "/reference-data/leafConcepts?conceptName="
                + URLEncoder.encode(TEMPLATE_NAME, "UTF-8"));
        when(httpClient.get(leafConceptsUri)).thenReturn(mapper.writeValueAsString(leafConcepts));

        URI conceptIdUri = new URI(root + "/reference-data/getConceptId?"
                + getConceptNamesParameter(Collections.singletonList(TEMPLATE_NAME)));
        when(httpClient.get(conceptIdUri)).thenReturn("[5000]");

        URI latestFormsUri = new URI(root + "/bahmniie/form/latestPublishedForms");
        when(httpClient.get(latestFormsUri)).thenReturn(
                FileUtils.readFileToString(new File("src/test/resources/forms/cannedLatestPublishedForms.json")));

        URI formUri = new URI(root + "/form/canned-form-uuid-5000?v=custom:(resources:(value))");
        when(httpClient.get(formUri)).thenReturn(
                FileUtils.readFileToString(new File("src/test/resources/forms/cannedVitalsForm.json")));

        List<ConceptName> formLeafConcepts = Arrays.asList(
                new ConceptName("Canned Height", null), new ConceptName("Canned Weight", null));
        String formLeafConceptsJson = mapper.writeValueAsString(formLeafConcepts);

        /* the formBuilder report asks for the leaves it parsed out of the form definition... */
        URI byLeafNamesUri = new URI(root + "/reference-data/leafConceptNames?"
                + getConceptNamesParameter(Arrays.asList("Canned Height", "Canned Weight")));
        when(httpClient.get(byLeafNamesUri)).thenReturn(formLeafConceptsJson);

        /* ...while the forms report asks for the same leaves by the form's own name. */
        URI byFormNameUri = new URI(root + "/reference-data/leafConceptNames?"
                + getConceptNamesParameter(Collections.singletonList(TEMPLATE_NAME)));
        when(httpClient.get(byFormNameUri)).thenReturn(formLeafConceptsJson);

        /* obsCount picks its template by fetching the concept's datatype over HTTP
         * (ConceptUtil.getConceptDataType), before it runs any SQL. */
        stubConceptDataType(root, "Canned Coded Question", "Coded");
        stubConceptDataType(root, "Canned Boolean Question", "Boolean");
    }

    private void stubConceptDataType(String root, String conceptName, String datatypeDisplay) throws Exception {
        URI conceptUri = new URI(new URI(null, root + "/concept/" + conceptName, null).toASCIIString());
        when(httpClient.get(conceptUri)).thenReturn(
                "{\"datatype\":{\"display\":\"" + datatypeDisplay + "\"}}");
    }

    /*
     * Executing without a SQL error is necessary but not sufficient, and that was measured
     * rather than assumed. Renaming a single column in concept_view (read by 15 of the report
     * SQL files) on a scratch copy of the test database turned 7 of these reports red with
     * "Unknown column 'dcn.concept_full_name' in 'field list'". It also silently took the
     * patient report from 3 rows to 0 with no error at all, because personReport.sql reaches
     * that column through an outer join. A no-error assertion would have passed that.
     *
     * So each config entry carries a minRows floor, taken from what the report returns
     * against the seeded data, and a report that quietly stops matching anything fails here
     * instead of passing. If a floor starts failing, compare the printed row counts below
     * against the ones in the previous run before assuming the floor is wrong.
     */
    @Test
    public void everyReportShouldExecuteAndStillReturnItsRows() throws Exception {
        assertCannedDataPresent();

        Map<String, String> failures = new LinkedHashMap<>();
        Map<String, Integer> rowCounts = new LinkedHashMap<>();

        for (JsonNode entry : configRoot()) {
            String reportName = entry.get("name").asText();
            /* Default 1, not 0: an entry that forgot its floor still has to return something.
             * smokeConfigShouldCoverEveryRegisteredReportType is what names the entry. */
            int minRows = entry.path("minRows").asInt(1);
            try {
                CsvReport report = fetchCsvReport(reportName, START_DATE, END_DATE, true);
                if (report.getErrorMessage() != null) {
                    failures.put(reportName, report.getErrorMessage());
                } else if (report.rowsCount() < minRows) {
                    failures.put(reportName, "returned " + report.rowsCount()
                            + " rows, expected at least " + minRows
                            + ". The SQL executed without error, so look for a column reached "
                            + "through an outer join that has changed or gone, a change in the "
                            + "seeded data, or a floor that was recorded too high.");
                } else {
                    rowCounts.put(reportName, report.rowsCount());
                }
            } catch (Exception e) {
                failures.put(reportName, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        /* printed on success too: the per-report row counts are what a future run compares
         * against when a minRows floor starts failing. */
        String summary = describe(failures, rowCounts);
        System.out.println(summary);
        assertTrue(summary, failures.isEmpty());
    }

    /*
     * Guards against the real failure mode of a list like this: someone registers a new report
     * type in Report.java and nothing notices that it is never executed against the schema.
     */
    @Test
    public void smokeConfigShouldCoverEveryRegisteredReportType() throws Exception {
        Set<String> configured = new TreeSet<>(typesFromConfig());
        Set<String> uncovered = new TreeSet<>();
        for (JsonSubTypes.Type type : Report.class.getAnnotation(JsonSubTypes.class).value()) {
            if (!configured.contains(type.name()) && !NOT_SMOKE_TESTABLE.contains(type.name())) {
                uncovered.add(type.name());
            }
        }
        assertTrue("report types registered in Report.java but never executed by the smoke "
                + "test. Add a config entry to " + CONFIG_PATH + ", or add the type to "
                + "NOT_SMOKE_TESTABLE with the reason: " + uncovered, uncovered.isEmpty());

        /*
         * Covering a type is only worth something if its entry asserts a row floor. Without
         * this, a new entry with no minRows satisfies both tests while checking nothing beyond
         * "the SQL parsed", which is the weaker assertion this class was written to avoid.
         */
        Set<String> withoutFloor = new TreeSet<>();
        for (JsonNode entry : configRoot()) {
            if (!entry.hasNonNull("minRows") || entry.get("minRows").asInt() < 1) {
                withoutFloor.add(entry.get("name").asText());
            }
        }
        assertTrue("smoke config entries with no minRows floor of at least 1. Run the report,"
                + " then record the row count it returns as its floor in " + CONFIG_PATH
                + ": " + withoutFloor, withoutFloor.isEmpty());
    }

    /*
     * A row floor only means something if the data it counts is there. If the fixture database
     * predates cannedReportsDataSet.xml -- created by an older create_db.sh, say -- every floor
     * fails at once, and the per-report message above would send the reader looking at report
     * SQL that is fine. Check the seeded data first, so a stale database says it is stale.
     */
    private void assertCannedDataPresent() throws Exception {
        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery("select count(*) from obs where obs_id >= 5000")) {
            rs.next();
            assertTrue("cannedReportsDataSet.xml seeded no observations, so every minRows floor "
                    + "below would fail for a reason that has nothing to do with the report SQL. "
                    + "Recreate the test databases with src/test/resources/create_db.sh, then "
                    + "rerun.", rs.getInt(1) > 0);
        }
    }

    private Set<String> typesFromConfig() throws Exception {
        Set<String> types = new HashSet<>();
        for (JsonNode entry : configRoot()) {
            types.add(entry.get("type").asText());
        }
        return types;
    }

    private Iterable<JsonNode> configRoot() throws Exception {
        JsonNode root = new ObjectMapper().readTree(new File(CONFIG_PATH));
        List<JsonNode> entries = new ArrayList<>();
        for (Iterator<JsonNode> it = root.elements(); it.hasNext(); ) {
            entries.add(it.next());
        }
        return entries;
    }

    private static String describe(Map<String, String> failures, Map<String, Integer> rowCounts) {
        StringBuilder sb = new StringBuilder();
        sb.append(failures.size()).append(" of ")
                .append(failures.size() + rowCounts.size())
                .append(" reports failed to execute against the OpenMRS schema:\n");
        for (Map.Entry<String, String> f : failures.entrySet()) {
            sb.append("  FAILED  ").append(f.getKey()).append("  ->  ").append(f.getValue()).append('\n');
        }
        for (Map.Entry<String, Integer> ok : rowCounts.entrySet()) {
            sb.append("  ok      ").append(ok.getKey())
                    .append("  (").append(ok.getValue()).append(" rows)\n");
        }
        return sb.toString();
    }
}
