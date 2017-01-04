package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericObservationFormDaoImpl;
import org.bahmni.reports.model.ConceptName;
import org.bahmni.reports.model.GenericObservationFormReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.webclients.WebClientsException;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddAgeGroupColumn;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddConceptColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddDataAnalysisColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddDefaultColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddExtraPatientIdentifierTypes;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddPatientAddressColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddPatientAttributeColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.createAndAddProgramsToFilterColumns;
import static org.bahmni.reports.util.GenericObservationFormReportTemplateHelper.fetchLeafConceptsAsList;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddProviderNameColumn;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddVisitInfoColumns;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.getListOfFullySpecifiedNames;
import static org.bahmni.reports.util.GenericReportsHelper.createAndAddVisitAttributeColumns;

@UsingDatasource("openmrs")
public class GenericObservationFormReportTemplate extends BaseReportTemplate<GenericObservationFormReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public GenericObservationFormReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<GenericObservationFormReportConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources,
                                     PageType pageType) throws SQLException, WebClientsException, InvalidConfigurationException {
        CommonComponents.addTo(jasperReport, report, pageType);


        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        List<String> formNamesToFilter = new ArrayList<>();
        createAndAddDefaultColumns(jasperReport, report.getConfig());
        if (report.getConfig() != null && (!CollectionUtils.isEmpty(report.getConfig().getFormNamesToFilter()))) {
           if(fetchLeafConceptsAsList(report, bahmniReportsProperties).size() != 0) {
               createAndAddExtraPatientIdentifierTypes(jasperReport, report.getConfig());
               createAndAddVisitAttributeColumns(jasperReport, report.getConfig());
               createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
               createAndAddProgramsToFilterColumns(jasperReport, report.getConfig());
               createAndAddPatientAddressColumns(jasperReport, report.getConfig());
               createAndAddProviderNameColumn(jasperReport, report.getConfig());
               createAndAddVisitInfoColumns(jasperReport, report.getConfig());
               List<ConceptName> leafConceptNames = fetchLeafConceptsAsList(report, bahmniReportsProperties);
               createAndAddConceptColumns(leafConceptNames, jasperReport, report.getConfig().getConceptNameDisplayFormat());
               formNamesToFilter = getListOfFullySpecifiedNames(leafConceptNames);
               createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
               createAndAddAgeGroupColumn(jasperReport, report.getConfig());
           }
           else{
               throw new InvalidConfigurationException("Please provide a valid Form Name");
           }
        } else {
            throw new InvalidConfigurationException("You need configure atleast one observation form to filter");
        }

        GenericDao genericObservationFormDao = new GenericObservationFormDaoImpl(report, bahmniReportsProperties);

        ResultSet obsResultSet = genericObservationFormDao.getResultSet(connection, startDate, endDate, formNamesToFilter);

        JasperReportBuilder jasperReportBuilder = obsResultSet != null ? jasperReport.setDataSource(obsResultSet) : jasperReport;
        return new BahmniReportBuilder(jasperReportBuilder);
    }
}
