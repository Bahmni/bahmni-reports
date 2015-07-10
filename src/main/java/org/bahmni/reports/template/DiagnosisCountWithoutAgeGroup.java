package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "DiagnosisCountWithoutAgeGroup")
public class DiagnosisCountWithoutAgeGroup{
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<DiagnosisReportConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) {
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        TextColumnBuilder<String> disease = col.column("Name of Disease", "disease", type.stringType());
        TextColumnBuilder<String> icd10Code = col.column("ICD Code", "icd10_code", type.stringType());
        TextColumnBuilder<String> female = col.column("Female", "female", type.stringType());
        TextColumnBuilder<String> male = col.column("Male", "male", type.stringType());
        TextColumnBuilder<String> other = col.column("Other", "other", type.stringType());

        String sql = getFileContent("sql/diagnosisCountWithoutAgeGroup.sql");

        jasperReport.setColumnStyle(textStyle)
                .columns(disease, icd10Code, female, male, other)
                .setDataSource(String.format(sql, startDate, endDate, reportConfig.getConfig().getVisitTypes()),
                        connection);
        return jasperReport;
    }
}
