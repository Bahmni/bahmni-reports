package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.model.VisitReportConfig;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class VisitReportTemplate extends BaseReportTemplate<VisitReportConfig> {

    private void addColumns(JasperReportBuilder jasperReport, List<String> attributes, StyleBuilder columnStyle) {
        for (String attribute : attributes) {
            TextColumnBuilder<String> attributeColumn = col.column(attribute, attribute, type.stringType())
                    .setStyle(columnStyle);
            jasperReport.addColumn(attributeColumn);
        }
    }

    private String getSqlString(String personAttributes, String visitAttributes, String startDate, String endDate) {
        String sql = getFileContent("sql/visit.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("personAttributes", personAttributes);
        sqlTemplate.add("visitAttributes", visitAttributes);
        return sqlTemplate.render();
    }

    private String sqlStringListParameter(List<String> params) {
        return "\"" + StringUtils.join(params, "\", \"") + "\"";
    }


    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<VisitReportConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) throws SQLException {

        String personAttributes = sqlStringListParameter(report.getConfig().getPersonAttributes());
        String visitAttributes = sqlStringListParameter(report.getConfig().getVisitAttributes());

        TextColumnBuilder<String> patientIdColumn = col.column("Patient Id", "identifier", type.stringType()).setStyle(minimalColumnStyle);
        TextColumnBuilder<String> patientNameColumn = col.column("Patient Name", "Patient Name", type.stringType()).setStyle
                (minimalColumnStyle);
        TextColumnBuilder<String> genderColumn = col.column("Gender", "Gender", type.stringType()).setStyle(minimalColumnStyle);

        jasperReport.setShowColumnTitle(true)
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
                .columns(patientIdColumn, patientNameColumn, genderColumn);

        addColumns(jasperReport, report.getConfig().getVisitAttributes(), minimalColumnStyle);
        addColumns(jasperReport, report.getConfig().getPersonAttributes(), minimalColumnStyle);

        String sqlString = getSqlString(personAttributes, visitAttributes, startDate, endDate);

        JasperReportBuilder jasperReportBuilder = SqlUtil.executeReportWithStoredProc(jasperReport, connection, sqlString);
        return new BahmniReportBuilder(jasperReportBuilder);
    }
}
