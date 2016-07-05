package org.bahmni.reports.template;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.impl.GenericProgramDaoImpl;
import org.bahmni.reports.model.GenericProgramReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;

import static org.bahmni.reports.util.GenericProgramReportTemplateHelper.*;

import org.bahmni.reports.util.CommonComponents;

@UsingDatasource("openmrs")
public class GenericProgramTemplate extends BaseReportTemplate<GenericProgramReportConfig> {
    private BahmniReportsProperties bahmniReportsProperties;

    public GenericProgramTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<GenericProgramReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);

         jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        createAndAddMandatoryColumns(jasperReport);
        createAndAddProgramStatesColumns(jasperReport, report.getConfig());
        if (report.getConfig() != null) {
            createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
            createAndAddProgramAttributeColumns(jasperReport, report.getConfig());
            createAndAddPatientAddressColumns(jasperReport, report.getConfig());
            createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
        }

        GenericProgramDaoImpl genericProgramDao = new GenericProgramDaoImpl(report);
        ResultSet programResultSet = genericProgramDao.getResultSet(connection, startDate, endDate, null);
        return programResultSet != null ? jasperReport.setDataSource(programResultSet) : jasperReport;
    }
}
