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
import org.bahmni.reports.api.model.ReportConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;

@Component(value = "ObsCountByGenderAndAge")
public class ObsCountByGenderAndAgeGroupTemplateBase implements BaseReportTemplate {

    @Autowired
    private javax.sql.DataSource dataSource;

    @Override
    public JasperReportBuilder build(ReportConfig reportConfig) throws SQLException {
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
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .title(cmp.text(reportConfig.getTitle()))
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(reportConfig.getName())
                .summary(crosstab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(getSql(reportConfig.getAgeGroupName(), reportConfig.getConceptName(), reportConfig.getStartDate(), reportConfig.getStopDate()), dataSource.getConnection());
        return report;
    }


    private static String getSql(final String ageGroupName, final String conceptFullName, final String startDate, final String stopDate) {
        return " SELECT distinct answer.concept_full_name as concept_name, " +
                " reporting_age_group.name AS age_group, " +
                " IF(required_obs.age_group_id IS NULL, 0, SUM(IF(required_obs.gender = 'F', 1, 0))) AS female_count, " +
                " IF(required_obs.age_group_id IS NULL, 0, SUM(IF(required_obs.gender = 'M', 1, 0))) AS male_count, " +
                " IF(required_obs.age_group_id IS NULL, 0, COUNT(obs_id)) as total_count " +
                " FROM concept_view as question " +
                " JOIN concept_answer ON question.concept_id = concept_answer.concept_id " +
                " JOIN concept_view as answer ON answer.concept_id = concept_answer.answer_concept " +
                " LEFT OUTER JOIN reporting_age_group ON reporting_age_group.report_group_name = '" + ageGroupName + "' " +
                " LEFT OUTER JOIN ( " +
                " SELECT distinct valid_coded_obs_view.obs_id, valid_coded_obs_view.concept_id, valid_coded_obs_view.value_coded, observed_age_group.id as age_group_id, person.person_id, person.gender " +
                " FROM valid_coded_obs_view  " +
                " LEFT OUTER JOIN person ON valid_coded_obs_view.person_id = person.person_id " +
                " LEFT OUTER JOIN reporting_age_group as observed_age_group ON observed_age_group.report_group_name = '" + ageGroupName + "' AND " +
                " valid_coded_obs_view.obs_datetime BETWEEN (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.min_years YEAR), INTERVAL observed_age_group.min_days DAY))  " +
                " AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.max_years YEAR), INTERVAL observed_age_group.max_days DAY))  " +
                " WHERE valid_coded_obs_view.obs_datetime BETWEEN '" + startDate + "' AND '" + stopDate + "' AND valid_coded_obs_view.concept_full_name = '" + conceptFullName + "' " +
                " ) AS required_obs ON required_obs.concept_id = question.concept_id AND required_obs.value_coded = answer.concept_id  " +
                "  AND reporting_age_group.id = required_obs.age_group_id " +
                " WHERE question.concept_full_name = '" + conceptFullName + "' " +
                " GROUP BY answer.concept_id, age_group " +
                " ORDER BY answer.concept_id, reporting_age_group.sort_order;";
    }
    
}
