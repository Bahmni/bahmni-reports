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
import org.bahmni.reports.model.ReportConfig;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "DiagnosisForOPDPatient")
public class DiagnosisForOPDPatientTemplate extends AbstractMRSReportTemplate {

    @Override
    public JasperReportBuilder buildReport(Connection connection, ReportConfig reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException {
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());
        StyleBuilder cellStyle = Templates.columnStyle.setBorder(Styles.pen());

        CrosstabColumnGroupBuilder<String> femaleCountGroup = ctab.columnGroup("something", String.class)
                .setShowTotal(false);

        CrosstabRowGroupBuilder<String> diseaseNameRowGroup = ctab.rowGroup("disease", String.class).setHeaderStyle(textStyle)
                .setShowTotal(false);
        CrosstabRowGroupBuilder<String> icd10RowGroup = ctab.rowGroup("icd10_code", String.class).setHeaderStyle(textStyle)
                .setShowTotal(false);

        TextColumnBuilder<String> disease = col.column("Name of Disease", "disease", type.stringType());
        TextColumnBuilder<String> icd10Code = col.column("ICD Code", "icd10_code", type.stringType());
        TextColumnBuilder<String> groupName = col.column("group Name", "group_name", type.stringType()).setStyle(Templates.bold12CenteredStyle.setBackgroundColor(Color.getHSBColor(31, 9, 98)));
        TextColumnBuilder<String> female = col.column("Female", "female", type.stringType());
        TextColumnBuilder<String> male = col.column("Male", "male", type.stringType());
        
        CrosstabBuilder crossTab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.horizontalList(DynamicReports.cmp.text("ICD Code").setStyle(Templates.columnTitleStyle),
                        DynamicReports.cmp.text("Disease Name").setStyle(Templates.columnTitleStyle)))
                .rowGroups(icd10RowGroup, diseaseNameRowGroup)
                .columnGroups(femaleCountGroup)
                .measures(
                        ctab.measure("Female", female, Calculation.NOTHING).setStyle(textStyle),
                        ctab.measure("Male", male, Calculation.NOTHING).setStyle(textStyle)
                )
                .setCellStyle(cellStyle);

        String sql = getFileContent("sql/diagnosisCountOPD.sql");
        


        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .summary(crossTab)
                .pageFooter(Templates.footerComponent)
                .columns(icd10Code, disease, groupName, female, male)
                .groupBy(groupName)
                .setDataSource(String.format(sql, startDate, endDate),
                        connection);
        return report;
    }

}
