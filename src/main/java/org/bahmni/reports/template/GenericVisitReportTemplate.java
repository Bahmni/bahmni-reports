package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.dao.impl.GenericVisitDaoImpl;
import org.bahmni.reports.model.GenericVisitReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.bahmni.reports.util.GenericReportsHelper.createAndAddExtraPatientIdentifierTypes;
import static org.bahmni.reports.util.GenericVisitReportTemplateHelper.*;

@UsingDatasource("openmrs")
public class GenericVisitReportTemplate extends BaseReportTemplate<GenericVisitReportConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<GenericVisitReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException, InvalidConfigurationException {
        CommonComponents.addTo(jasperReport, report, pageType);

        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        createAndAddMandatoryColumns(jasperReport);
        if (report.getConfig() != null) {
            createAndAddExtraPatientIdentifierTypes(jasperReport, report.getConfig());
            createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
            createAndAddPatientAddressColumns(jasperReport, report.getConfig());
            createAndAddVisitAttributeColumns(jasperReport, report.getConfig());
            createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
            createAndAddAgeGroupColumn(jasperReport,report.getConfig());
        }

        GenericVisitDaoImpl genericVisitDao = new GenericVisitDaoImpl(report);
        ResultSet obsResultSet = genericVisitDao.getResultSet(connection, startDate, endDate, null);

        JasperReportBuilder jasperReportBuilder = obsResultSet != null ? jasperReport.setDataSource(obsResultSet) : jasperReport;
        return new BahmniReportBuilder(jasperReportBuilder);
    }
}
