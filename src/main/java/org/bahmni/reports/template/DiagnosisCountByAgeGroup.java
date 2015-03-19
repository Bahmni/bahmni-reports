package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.OrderType;
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

@Component
@UsingDatasource("openmrs")
public class DiagnosisCountByAgeGroup{
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<DiagnosisReportConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());
        StyleBuilder cellStyle = Templates.columnStyle.setBorder(Styles.pen());

        CrosstabRowGroupBuilder<String> diseaseNameRowGroup = ctab.rowGroup("disease", String.class).setHeaderStyle(textStyle).setHeaderWidth(120)
                .setShowTotal(false);
        CrosstabRowGroupBuilder<String> icd10RowGroup = ctab.rowGroup("ICD Code", String.class).setHeaderStyle(textStyle).setHeaderWidth(60)
                .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> ageColumnGroup = ctab.columnGroup("age_group", String.class).setTotalHeaderWidth(95)
                .setShowTotal(false);
        CrosstabColumnGroupBuilder<Integer> ageSortOrderColumnGroup = ctab.columnGroup("age_group_sort_order", Integer.class)
                .setShowTotal(false).setOrderType(OrderType.ASCENDING);

        CrosstabBuilder crossTab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.horizontalList(DynamicReports.cmp.text("Disease Name").setStyle(Templates.columnTitleStyle).setWidth(120),
                        DynamicReports.cmp.text("ICD Code").setStyle(Templates.columnTitleStyle).setWidth(60)))
                .rowGroups(diseaseNameRowGroup, icd10RowGroup)
                .columnGroups(ageSortOrderColumnGroup, ageColumnGroup)
                .measures(
                        ctab.measure("F", "female", Integer.class, Calculation.NOTHING).setStyle(textStyle),
                        ctab.measure("M", "male", Integer.class, Calculation.NOTHING).setStyle(textStyle),
                        ctab.measure("O", "other", Integer.class, Calculation.NOTHING).setStyle(textStyle)
                )
                .setCellStyle(cellStyle).setCellWidth(95);

        String sql = getFileContent("sql/diagnosisCountByAgeGroup.sql");

        jasperReport.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .summary(crossTab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, reportConfig.getConfig().getAgeGroupName(), startDate, endDate,
                        reportConfig.getConfig().getVisitTypes()), connection);
        return jasperReport;
    }
}
