package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang.StringUtils;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Config;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "DiagnosisCountWithoutAgeGroup")
public class DiagnosisCountWithoutAgeGroup{
    public JasperReportBuilder build(Connection connection, Report<DiagnosisReportConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException {
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        TextColumnBuilder<String> disease = col.column("Name of Disease", "disease", type.stringType());
        TextColumnBuilder<String> icd10Code = col.column("ICD Code", "icd10_code", type.stringType());
        TextColumnBuilder<String> female = col.column("Female", "female", type.stringType());
        TextColumnBuilder<String> male = col.column("Male", "male", type.stringType());
        TextColumnBuilder<String> other = col.column("Other", "other", type.stringType());

        String sql = getFileContent("sql/diagnosisCountWithoutAgeGroup.sql");

        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .pageFooter(Templates.footerComponent)
                .columns(disease, icd10Code, female, male, other)
                .setDataSource(String.format(sql, startDate, endDate, reportConfig.getConfig().getVisitTypes()),
                        connection);
        return report;
    }
}
