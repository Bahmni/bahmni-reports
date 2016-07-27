package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.column.DRColumn;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericObservationDaoImpl;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.GenericObservationReportTemplateHelper;

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
                                     PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);


        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        List<String> conceptNamesToFilter = new ArrayList<>();

        GenericObservationReportTemplateHelper.createAndAddDefaultColumns(jasperReport, report.getConfig());
        if (report.getConfig() != null) {
            createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
            createAndAddVisitAttributeColumns(jasperReport, report.getConfig());
            createAndAddPatientAddressColumns(jasperReport, report.getConfig());
            createAndAddProviderNameColumn(jasperReport, report.getConfig());
            createAndAddVisitInfoColumns(jasperReport, report.getConfig());
            if (report.getConfig().isEncounterPerRow()) {
                conceptNamesToFilter = fetchLeafConceptsAsList(report, bahmniReportsProperties);
                createAndAddConceptColumns(conceptNamesToFilter, jasperReport);
            }
            createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
            if (CollectionUtils.isNotEmpty(report.getConfig().getExcludeColumns())) {
                List<DRColumn<?>> filteredColumns = filterExcludedColumns(jasperReport.getReport().getColumns(), report.getConfig().getExcludeColumns());
                jasperReport.getReport().setColumns(filteredColumns);
            }
        }

        if (jasperReport.getReport().getColumns().size() == 0) {
            throw new IllegalArgumentException("You have excluded all columns.");
        }

        GenericDao genericObservationDao = new GenericObservationDaoImpl(report, bahmniReportsProperties);

        ResultSet obsResultSet = genericObservationDao.getResultSet(connection, startDate, endDate, conceptNamesToFilter);

        JasperReportBuilder jasperReportBuilder = obsResultSet != null ? jasperReport.setDataSource(obsResultSet) : jasperReport;
        return new BahmniReportBuilder(jasperReportBuilder);
    }

    private List<DRColumn<?>> filterExcludedColumns(List<DRColumn<?>> columns, List<String> excludeColumns) {
        List<DRColumn<?>> columnsToAdd = new ArrayList<>();
        for (DRColumn<?> column : columns) {
            if (!excludeColumns.contains(column.getName())) {
                columnsToAdd.add(column);
            }
        }
        return columnsToAdd;
    }

}
