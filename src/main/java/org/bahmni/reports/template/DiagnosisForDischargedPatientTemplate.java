package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@Component(value = "DiagnosisForDischargedPatient")
@UsingDatasource("openmrs")
public class DiagnosisForDischargedPatientTemplate implements BaseReportTemplate<DiagnosisReportConfig> {

    @Override
    public JasperReportBuilder build(Connection connection, Report<DiagnosisReportConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());
        StyleBuilder cellStyle = Templates.columnStyle.setBorder(Styles.pen());

        CrosstabRowGroupBuilder<String> diseaseNameRowGroup = ctab.rowGroup("disease", String.class).setHeaderStyle(textStyle)
                .setShowTotal(false);
        CrosstabRowGroupBuilder<String> icd10RowGroup = ctab.rowGroup("icd10_code", String.class).setHeaderStyle(textStyle)
                .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> ageColumnGroup = ctab.columnGroup("age_group", String.class)
                .setShowTotal(false);

        CrosstabBuilder crossTab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.horizontalList(DynamicReports.cmp.text("ICD Code").setStyle(Templates.columnTitleStyle),
                        DynamicReports.cmp.text("Disease Name").setStyle(Templates.columnTitleStyle)))
                .rowGroups(icd10RowGroup, diseaseNameRowGroup)
                .columnGroups(ageColumnGroup)
                .measures(
                        ctab.measure("Female", "female", Integer.class, Calculation.NOTHING).setStyle(textStyle),
                        ctab.measure("Male", "male", Integer.class, Calculation.NOTHING).setStyle(textStyle)
                )
                .setCellStyle(cellStyle);

        String sql = getFileContent("sql/diagnosisCount.sql");

        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .summary(crossTab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, reportConfig.getConfig().getAgeGroupName(), startDate, endDate),
                        connection);
        return report;
    }
}
