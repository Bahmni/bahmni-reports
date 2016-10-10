package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericObservationDaoImpl;
import org.bahmni.reports.model.ConceptName;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.webclients.WebClientsException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.*;

@UsingDatasource("openmrs")
public class GenericObservationReportTemplate extends BaseReportTemplate<GenericObservationReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public GenericObservationReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<GenericObservationReportConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources,
                                     PageType pageType) throws SQLException,WebClientsException {
        CommonComponents.addTo(jasperReport, report, pageType);


        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        List<String> conceptNamesToFilter = new ArrayList<>();
        List<String> allTheColumnsForReport = new ArrayList<>();

        createAndAddDefaultColumns(allTheColumnsForReport,report.getConfig());
        if (report.getConfig() != null) {
            allTheColumnsForReport.addAll(getExtraPatientIdentifierTypes(report.getConfig()));
            allTheColumnsForReport.addAll(getPatientAttributes(report.getConfig()));
            allTheColumnsForReport.addAll(getVisitAttributes(report.getConfig()));
            allTheColumnsForReport.addAll(getPatientAddresses(report.getConfig()));
            createAndAddProviderNameColumn(allTheColumnsForReport, report.getConfig());
            createAndAddDataAnalysisColumns(allTheColumnsForReport, report.getConfig());
            if (report.getConfig().isEncounterPerRow()) {
                List<ConceptName> leafConceptNames = fetchLeafConceptsAsList(report, bahmniReportsProperties);
                createAndAddConceptColumns(allTheColumnsForReport,leafConceptNames, report.getConfig().getConceptNameDisplayFormat());
                conceptNamesToFilter = getListOfFullySpecifiedNames(leafConceptNames);
            }

            createAndAddAgeGroupColumn(allTheColumnsForReport, report.getConfig());
            createAndAddVisitInfoColumns(allTheColumnsForReport, report.getConfig());
        }
        addColumnsToReport(jasperReport, allTheColumnsForReport, report.getConfig());

        GenericDao genericObservationDao = new GenericObservationDaoImpl(report, bahmniReportsProperties);

        ResultSet obsResultSet = genericObservationDao.getResultSet(connection, startDate, endDate, conceptNamesToFilter);

        JasperReportBuilder jasperReportBuilder = obsResultSet != null ? jasperReport.setDataSource(obsResultSet) : jasperReport;
        return new BahmniReportBuilder(jasperReportBuilder);
    }
}
