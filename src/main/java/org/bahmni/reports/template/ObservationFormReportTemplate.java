package org.bahmni.reports.template;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.ObservationFormDao;
import org.bahmni.reports.dao.impl.ObservationFormDaoImpl;
import org.bahmni.reports.model.*;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.WebClientsException;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddAgeGroupColumn;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddConceptColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddDataAnalysisColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddDefaultColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddExtraPatientIdentifierTypes;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddPatientAddressColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddPatientAttributeColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.*;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.*;
import static org.bahmni.reports.util.GenericReportsHelper.createAndAddVisitAttributeColumns;

@UsingDatasource("openmrs")
public class ObservationFormReportTemplate extends BaseReportTemplate<GenericObservationFormReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public ObservationFormReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<GenericObservationFormReportConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources,
                                     PageType pageType) throws SQLException, WebClientsException, InvalidConfigurationException, URISyntaxException, IOException {
        CommonComponents.addTo(jasperReport, report, pageType);


        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);


        List<ConceptName> conceptNamesToFilter = new ArrayList<>();
        List<String> formVersionsList = new ArrayList<>();

        if (report.getConfig() == null || (CollectionUtils.isEmpty(report.getConfig().getFormNamesToFilter()))) {
            throw new InvalidConfigurationException("You need configure atleast one observation form to filter");
        }

        List<BahmniForm> formList = getFormList(report, bahmniReportsProperties);

        if(formList.isEmpty()) {
            throw new InvalidConfigurationException("Please provide a valid Form Name");
        }

        addConcepts(report, bahmniReportsProperties, formList, formVersionsList, conceptNamesToFilter);
        createAndAddDefaultColumns(jasperReport, report.getConfig());
        createAndAddExtraPatientIdentifierTypes(jasperReport, report.getConfig());
        createAndAddVisitAttributeColumns(jasperReport, report.getConfig());
        createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
        createAndAddProgramsToFilterColumns(jasperReport, report.getConfig());
        createAndAddPatientAddressColumns(jasperReport, report.getConfig());
        createAndAddProviderNameColumn(jasperReport, report.getConfig());
        createAndAddVisitInfoColumns(jasperReport, report.getConfig());
        createAndAddConceptColumns(conceptNamesToFilter, jasperReport, report.getConfig().getConceptNameDisplayFormat());
        createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
        createAndAddAgeGroupColumn(jasperReport, report.getConfig());

        List<String> conceptNames = getListOfFullySpecifiedNames(conceptNamesToFilter);
        ObservationFormDao observationFormDao = new ObservationFormDaoImpl(report);
        ResultSet obsResultSet = observationFormDao.getResultSet(connection, startDate, endDate, conceptNames, formVersionsList);
        JasperReportBuilder jasperReportBuilder = obsResultSet != null ? jasperReport.setDataSource(obsResultSet) : jasperReport;
        return new BahmniReportBuilder(jasperReportBuilder);
    }

    private List<BahmniForm> getFormList(Report<GenericObservationFormReportConfig> report, BahmniReportsProperties bahmniReportsProperties)
            throws URISyntaxException, IOException {
        List<String> formNamesToFilter = report.getConfig().getFormNamesToFilter();
        HttpClient httpClient = report.getHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String latestFormsUrl = bahmniReportsProperties.getOpenmrsRootUrl() + "/bahmniie/form/latestPublishedForms";

        String result = httpClient.get(new URI(latestFormsUrl));

        List<BahmniForm> formList = mapper.readValue(result, new TypeReference<List<BahmniForm>>() {});

        return formList.stream()
                .filter(form -> formNamesToFilter.stream()
                        .anyMatch(f -> f.equalsIgnoreCase(form.getName())))
                .collect(Collectors.toList());

    }

    private void addConcepts(Report<GenericObservationFormReportConfig> report, BahmniReportsProperties bahmniReportsProperties,
                                    List<BahmniForm> formList, List<String> formVersionsList, List<ConceptName> conceptNamestoFilter) throws URISyntaxException {

        HttpClient httpClient = report.getHttpClient();

        for(BahmniForm form: formList) {

            formVersionsList.add(form.getName() + "." + form.getVersion());
            String url = bahmniReportsProperties.getOpenmrsRootUrl()
                    + "/form/" + form.getUuid()+ "?v=custom:(resources:(value))";
            String json = httpClient.get(new URI(url));

            JSONArray read = JsonPath.read(json, "$..value");
            ReadContext parse = JsonPath.parse(read.get(0).toString());
            List<Map<String, String>> concepts = parse.read("$..concept");

            List<String> conceptSets = new ArrayList<>();
            for(Map<String, String> concept: concepts){
                String conceptName = concept.get("name");
                if (concept.get("units") != null) {
                    conceptName = conceptName.replace("(" + concept.get("units") + ")", "");
                }
                conceptSets.add(conceptName);
            }

            List<ConceptName> leafConcepts = conceptSets.isEmpty() ?
                    new ArrayList<>() : fetchConcepts(conceptSets, report.getHttpClient(), bahmniReportsProperties);

            for(ConceptName conceptName: leafConcepts){
                if(!conceptNamestoFilter.contains(conceptName)){
                    conceptNamestoFilter.add(conceptName);
                }
            }
        }
    }


}
