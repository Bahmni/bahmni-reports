package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.column.DRColumn;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericLabOrderDaoImpl;
import org.bahmni.reports.model.GenericLabOrderReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.GenericLabOrderReportTemplateHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.bahmni.reports.util.GenericLabOrderReportTemplateHelper.*;

@UsingDatasource("openmrs")
public class GenericLabOrderReportTemplate extends BaseReportTemplate<GenericLabOrderReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public GenericLabOrderReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReportBuilder,
                                     Report<GenericLabOrderReportConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources,
                                     PageType pageType) throws Exception {
        CommonComponents.addTo(jasperReportBuilder, report, pageType);
        jasperReportBuilder.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        List<String> conceptNamesToFilter = new ArrayList<>();
        GenericLabOrderReportTemplateHelper.createAndAddMandatoryColumns(jasperReportBuilder, report.getConfig());
        showOrderDateTime(jasperReportBuilder, report.getConfig());
        if (report.getConfig() != null) {
            createAndAddPatientAttributeColumns(jasperReportBuilder, report.getConfig());
            createAndAddVisitAttributeColumns(jasperReportBuilder, report.getConfig());
            createAndAddPatientAddressColumns(jasperReportBuilder, report.getConfig());
            createAndAddProviderNameColumn(jasperReportBuilder, report.getConfig());
            createAndAddVisitInfoColumns(jasperReportBuilder, report.getConfig());
            createAndAddProgramNameColumn(jasperReportBuilder, report.getConfig());
            createAndAddDataAnalysisColumns(jasperReportBuilder, report.getConfig());
        }

        removeExcludedColumns(jasperReportBuilder, report);

        if (jasperReportBuilder.getReport().getColumns().size() == 0) {
            throw new Exception("You have excluded all columns");
        }

        GenericDao genericObservationDao = new GenericLabOrderDaoImpl(report, bahmniReportsProperties);
        ResultSet obsResultSet = genericObservationDao.getResultSet(connection, startDate, endDate, conceptNamesToFilter);
        jasperReportBuilder = obsResultSet != null ? jasperReportBuilder.setDataSource(obsResultSet) : jasperReportBuilder;
        return new BahmniReportBuilder(jasperReportBuilder);
    }

    private void removeExcludedColumns(JasperReportBuilder jasperReportBuilder, Report<GenericLabOrderReportConfig> report) {
        GenericLabOrderReportConfig config = report.getConfig();
        if (config == null || config.getExcludeColumns() == null)
            return;

        List<DRColumn<?>> columns = jasperReportBuilder.getReport().getColumns();
        List<String> excludedColumns = report.getConfig().getExcludeColumns();
        List<DRColumn<?>> columnsToAdd = new ArrayList<>();

        for (DRColumn<?> column : columns) {
            if (!excludedColumns.contains(column.getName()))
                columnsToAdd.add(column);
        }
        jasperReportBuilder.getReport().setColumns(columnsToAdd);
    }
}
