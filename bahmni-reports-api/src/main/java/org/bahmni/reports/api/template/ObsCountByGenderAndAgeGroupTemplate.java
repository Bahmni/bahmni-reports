package org.bahmni.reports.api.template;

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
import org.bahmni.reports.api.Templates;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.api.FileReaderUtil.getFileContent;

@Component(value = "ObsCountByGenderAndAge")
public class ObsCountByGenderAndAgeGroupTemplate implements BaseReportTemplate {

    @Autowired
    private javax.sql.DataSource dataSource;

    @Override
    public JasperReportBuilder build(JSONObject reportConfig, String startDate, String endDate) throws SQLException {
        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("age_group", String.class)
                .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("concept_name", String.class)
                .setShowTotal(false);

        CrosstabBuilder crosstab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.text("Age Group / Outcome"))
                .rowGroups(rowGroup)
                .columnGroups(columnGroup)
                .measures(
                        ctab.measure("Female", "female_count", Integer.class, Calculation.NOTHING),
                        ctab.measure("Male", "male_count", Integer.class, Calculation.NOTHING),
                        ctab.measure("Total", "total_count", Integer.class, Calculation.NOTHING)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        String sql = getFileContent("sql/ageGroupNameQuery.txt");

        String ageGroupName = (String) reportConfig.get("ageGroupName");
        String conceptName = (String) reportConfig.get("conceptName");

        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .title(cmp.text((String) reportConfig.get("name")))
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName((String) reportConfig.get("name"))
                .summary(crosstab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(String.format(sql, ageGroupName, ageGroupName, startDate, endDate, conceptName, conceptName),
                        dataSource.getConnection());
        return report;
    }

}
