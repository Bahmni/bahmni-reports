package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.bahmni.reports.util.GenericObservationReportTemplateHelper;
import org.bahmni.webclients.HttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.stringtemplate.v4.ST;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.*;

@UsingDatasource("openmrs")
public class GenericObservationReportTemplate extends BaseReportTemplate<GenericObservationReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public GenericObservationReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<GenericObservationReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);

        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        List<String> conceptNamesToFilter = new ArrayList<>();

        GenericObservationReportTemplateHelper.createAndAddMandatoryColumns(jasperReport, report.getConfig());
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
        }

        String formattedSql = getFormattedSql(report, startDate, endDate, conceptNamesToFilter);
        return SqlUtil.executeReportWithStoredProc(jasperReport, connection, formattedSql);
    }

    private String getFormattedSql(Report<GenericObservationReportConfig> report, String startDate, String endDate, List<String> conceptNamesToFilter) {
        String sql;
        if (report.getConfig() != null && report.getConfig().isEncounterPerRow()) {
            sql = getFileContent("sql/genericObservationReportInOneRow.sql");
        } else {
            sql = getFileContent("sql/genericObservationReport.sql");
        }
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        if (report.getConfig() != null) {
            sqlTemplate.add("patientAttributes", constructPatientAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("patientAddresses", constructPatientAddressesToDisplay(report.getConfig()));
            sqlTemplate.add("visitAttributes", constructVisitAttributeNamesToDisplay(report.getConfig()));
            sqlTemplate.add("locationTagsToFilter", constructLocationTagsToFilter(report.getConfig()));
            sqlTemplate.add("conceptClassesToFilter", constructConceptClassesToFilter(report.getConfig()));
            sqlTemplate.add("programsToFilter", constructProgramsString(report.getConfig()));
            sqlTemplate.add("conceptNamesToFilter", constructConceptNamesToFilter(report, bahmniReportsProperties));
            sqlTemplate.add("selectConceptNamesSql", constructConceptNameSelectSqlIfShowInOneRow(conceptNamesToFilter, report.getConfig()));
            sqlTemplate.add("showProvider", report.getConfig().showProvider());
        }
        sqlTemplate.add("applyDateRangeFor", getDateRangeFor(report.getConfig()));
        return sqlTemplate.render();
    }
}
