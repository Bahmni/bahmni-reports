package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericObservationDaoImpl;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddConceptColumns;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddDataAnalysisColumns;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddDefaultColumns;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddPatientAddressColumns;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddPatientAttributeColumns;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddProviderNameColumn;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddVisitAttributeColumns;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.createAndAddVisitInfoColumns;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.fetchLeafConceptsAsList;
import static org.bahmni.reports.util.GenericReportsHelper.createAndAddExtraPatientIdentifierTypes;

@UsingDatasource("openmrs")
public class GenericObservationReportTemplate extends BaseReportTemplate<GenericObservationReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public GenericObservationReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReportBuilder, Report<GenericObservationReportConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReportBuilder, report, pageType);

        jasperReportBuilder.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        List<String> conceptNamesToFilter = new ArrayList<>();
        createAndAddDefaultColumns(jasperReportBuilder, report.getConfig());
        if (report.getConfig() != null) {
            createAndAddExtraPatientIdentifierTypes(jasperReportBuilder, report.getConfig());
            createAndAddPatientAttributeColumns(jasperReportBuilder, report.getConfig());
            createAndAddVisitAttributeColumns(jasperReportBuilder, report.getConfig());
            createAndAddPatientAddressColumns(jasperReportBuilder, report.getConfig());
            createAndAddProviderNameColumn(jasperReportBuilder, report.getConfig());
            createAndAddVisitInfoColumns(jasperReportBuilder, report.getConfig());
            if (report.getConfig().isEncounterPerRow()) {
                conceptNamesToFilter = fetchLeafConceptsAsList(report, bahmniReportsProperties);
                createAndAddConceptColumns(conceptNamesToFilter, jasperReportBuilder);
            }
            createAndAddDataAnalysisColumns(jasperReportBuilder, report.getConfig());
        }

        GenericDao genericObservationDao = new GenericObservationDaoImpl(report, bahmniReportsProperties);
        ResultSet obsResultSet = genericObservationDao.getResultSet(connection, startDate, endDate, conceptNamesToFilter);
        jasperReportBuilder = obsResultSet != null ? jasperReportBuilder.setDataSource(obsResultSet) : jasperReportBuilder;
        return new BahmniReportBuilder(jasperReportBuilder);
    }
}
