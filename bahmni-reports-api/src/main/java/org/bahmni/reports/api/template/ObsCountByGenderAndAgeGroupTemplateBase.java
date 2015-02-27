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
import org.apache.log4j.Logger;
import org.bahmni.reports.api.Templates;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.SQLException;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

@Component(value = "ObsCountByGenderAndAge")
public class ObsCountByGenderAndAgeGroupTemplateBase implements BaseReportTemplate {

    @Autowired
    private javax.sql.DataSource dataSource;

    private static final Logger logger = Logger.getLogger(ObsCountByGenderAndAgeGroupTemplateBase.class);

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

        JasperReportBuilder report = report();
        String sql = getSql((String) reportConfig.get("ageGroupName"), (String) reportConfig.get("conceptName"), startDate, endDate);
        logger.error(sql);
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .title(cmp.text((String) reportConfig.get("title")))
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName((String) reportConfig.get("name"))
                .summary(crosstab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(sql, dataSource.getConnection());
        return report;
    }


    private String getSql(final String ageGroupName, final String conceptFullName, final String startDate, final String stopDate) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("sql/ageGroupNameQuery.txt")));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return String.format(sb.toString(), ageGroupName, ageGroupName, startDate, stopDate, conceptFullName, conceptFullName);
        } catch (IOException e) {
            logger.error("File not found", e);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }
    
}
