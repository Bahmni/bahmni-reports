package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.TSIntegrationDiagnosisCountReportConfig;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.webclients.HttpClient;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.template.Templates.columnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;


@UsingDatasource("openmrs")
public class TSIntegrationDiagnosisCountReportTemplate extends BaseReportTemplate<TSIntegrationDiagnosisCountReportConfig> implements TSIntegrationDiagnosisService {
    public static final String DIAGNOSIS_COLUMN_NAME = "Diagnosis";
    public static final String TERMINOLOGY_COLUMN_NAME = "Terminology Code";
    public static final String FEMALE_COLUMN_NAME = "Female";
    public static final String MALE_COLUMN_NAME = "Male";
    public static final String OTHER_COLUMN_NAME = "Other";
    public static final String NOT_DISCLOSED_COLUMN_NAME = "Not disclosed";
    public static final String TOTAL_COLUMN_NAME = "Total";
    public static final String COUNT_COLUMN_NAME = "Count";
    public static final String SHORT_DISPLAY_FORMAT = "SHORT";
    public static final String FULLY_SPECIFIED_DISPLAY_FORMAT = "FULLY_SPECIFIED";
    private HttpClient httpClient;
    private Properties tsProperties;
    private String descendantsUrlTemplate;

    public TSIntegrationDiagnosisCountReportTemplate(HttpClient httpClient, Properties tsProperties, String descendantsUrlTemplate) {
        super();
        this.httpClient = httpClient;
        this.tsProperties = tsProperties;
        this.descendantsUrlTemplate = descendantsUrlTemplate;
    }

    public void setDescendantsUrlTemplate(String descendantsUrlTemplate) {
        this.descendantsUrlTemplate = descendantsUrlTemplate;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<TSIntegrationDiagnosisCountReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        String tempTableName = "tmpCodes_" + System.nanoTime();
        loadTempTable(connection, tempTableName, report.getConfig().getTerminologyParentCode(), tsProperties, descendantsUrlTemplate, httpClient);
        String sql = getFileContent("sql/tsIntegrationDiagnosisCount.sql");

        CommonComponents.addTo(jasperReport, report, pageType);
        jasperReport.addColumn(col.column(DIAGNOSIS_COLUMN_NAME, DIAGNOSIS_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        if (report.getConfig().isDisplayTerminologyCode()) {
            String terminologyConfigColumnName = report.getConfig().getTerminologyColumnName();
            String terminologyColumnName = StringUtils.isNotBlank(terminologyConfigColumnName) ? terminologyConfigColumnName : TERMINOLOGY_COLUMN_NAME;
            jasperReport.addColumn(col.column(terminologyColumnName, TERMINOLOGY_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        }
        if (report.getConfig().isDisplayGenderGroup()) {
            jasperReport.addColumn(col.column(FEMALE_COLUMN_NAME, FEMALE_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
            jasperReport.addColumn(col.column(MALE_COLUMN_NAME, MALE_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
            jasperReport.addColumn(col.column(OTHER_COLUMN_NAME, OTHER_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
            jasperReport.addColumn(col.column(NOT_DISCLOSED_COLUMN_NAME, NOT_DISCLOSED_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
        }
        TextColumnBuilder<Integer> rowCount = col.column(COUNT_COLUMN_NAME, TOTAL_COLUMN_NAME, type.integerType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        jasperReport.addColumn(rowCount);
        StyleBuilder subtotalStyle = stl.style().bold().setHorizontalAlignment(HorizontalAlignment.RIGHT);
        AggregationSubtotalBuilder<Integer> totalCount = sbt.sum(rowCount)
                .setLabel(TOTAL_COLUMN_NAME)
                .setLabelStyle(subtotalStyle);
        String formattedSql = getFormattedSql(sql, report.getConfig().getTsConceptSource(), report.getConfig().getConceptNameDisplayFormat(), startDate, endDate, tempTableName);
        jasperReport.setShowColumnTitle(true).setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL).subtotalsAtSummary(totalCount).setDataSource(formattedSql, connection);

        return new BahmniReportBuilder(jasperReport);
    }


    private String getFormattedSql(String templateSql, String conceptSourceCode, String conceptNameDisplayFormat, String startDate, String endDate, String tempTableName) {
        ST sqlTemplate = new ST(templateSql, '#', '#');
        sqlTemplate.add("conceptSourceCode", conceptSourceCode);
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("tempTable", tempTableName);
        sqlTemplate.add("conceptNameDisplayFormat", "shortNamePreferred".equals(conceptNameDisplayFormat) ? SHORT_DISPLAY_FORMAT : FULLY_SPECIFIED_DISPLAY_FORMAT);
        return sqlTemplate.render();
    }


}
