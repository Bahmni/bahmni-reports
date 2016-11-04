package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.impl.GenericProgramDaoImpl;
import org.bahmni.reports.model.GenericProgramReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.bahmni.reports.util.GenericProgramReportTemplateHelper.*;
import static org.bahmni.reports.util.GenericReportsHelper.createAndAddExtraPatientIdentifierTypes;

@UsingDatasource("openmrs")
public class GenericProgramTemplate extends BaseReportTemplate<GenericProgramReportConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<GenericProgramReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException, InvalidConfigurationException {
        CommonComponents.addTo(jasperReport, report, pageType);

        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        createAndAddMandatoryColumns(jasperReport);
        createAndAddProgramStatesColumns(jasperReport, report.getConfig());
        if (report.getConfig() != null) {
            createAndAddExtraPatientIdentifierTypes(jasperReport, report.getConfig());
            createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
            createAndAddProgramAttributeColumns(jasperReport, report.getConfig());
            createAndAddPatientAddressColumns(jasperReport, report.getConfig());
            createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
            createAndAddAgeGroupColumn(jasperReport, report.getConfig());
        }

        GenericProgramDaoImpl genericProgramDao = new GenericProgramDaoImpl(report);
        ResultSet programResultSet = genericProgramDao.getResultSet(connection, startDate, endDate, null);
        JasperReportBuilder jasperReportBuilder = programResultSet != null ? jasperReport.setDataSource(programResultSet) : jasperReport;
        return new BahmniReportBuilder(jasperReport);
    }
}
