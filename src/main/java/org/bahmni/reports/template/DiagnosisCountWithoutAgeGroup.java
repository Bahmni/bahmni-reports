package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class DiagnosisCountWithoutAgeGroup extends BaseReportTemplate<DiagnosisReportConfig> {

    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<DiagnosisReportConfig> reportConfig,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, reportConfig, pageType);

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        TextColumnBuilder<String> disease = col.column("Name of Disease", "disease", type.stringType());
        TextColumnBuilder<String> icd10Code = col.column("ICD Code", "icd10_code", type.stringType());
        TextColumnBuilder<String> female = col.column("Female", "female", type.stringType());
        TextColumnBuilder<String> male = col.column("Male", "male", type.stringType());
        TextColumnBuilder<String> other = col.column("Other", "other", type.stringType());

        String sql = getFileContent("sql/diagnosisCountWithoutAgeGroup.sql");

        jasperReport.setColumnStyle(textStyle)
                .columns(disease, icd10Code, female, male, other)
                .setDataSource(getFormattedSql(sql, reportConfig.getConfig(), startDate, endDate),
                        connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, DiagnosisReportConfig reportConfig, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("visitTypes", SqlUtil.toCommaSeparatedSqlString(reportConfig.getVisitTypes()));
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        String locationTagNames = SqlUtil.toCommaSeparatedSqlString(reportConfig.getLocationTagNames());
        String countOnlyTaggedLocationsJoin = String.format("INNER JOIN " +
                "(SELECT DISTINCT location_id " +
                " FROM location_tag_map INNER JOIN location_tag ON location_tag_map.location_tag_id = location_tag.location_tag_id " +
                " AND location_tag.name IN (%s)) locations ON locations.location_id = e.location_id", locationTagNames);

        if (StringUtils.isNotBlank(locationTagNames)) {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", countOnlyTaggedLocationsJoin);
        } else {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", "");
        }
        if (reportConfig.getIcd10ConceptSource() != null) {
            sqlTemplate.add("icd10ConceptSource", reportConfig.getIcd10ConceptSource());
        } else {
            sqlTemplate.add("icd10ConceptSource", "ICD 10 - WHO" );
        }
        return sqlTemplate.render();
    }
}
