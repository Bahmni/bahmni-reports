package org.bahmni.dhis.controller;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;

@Controller
public class ObservationsCountController {
    private static Logger logger = Logger.getLogger(ObservationsCountController.class);

    @Autowired
    private DataSource dataSource;

    @RequestMapping(value = "/obsReport", method = RequestMethod.GET)
    @ResponseBody
    public void generateReport(HttpServletResponse response) throws Exception {
        try {
            writeExcelToResponse(response);
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }
    }

    private void writeExcelToResponse(HttpServletResponse response) throws IOException, DRException, SQLException {
        JasperReportBuilder report = report();

        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("age_group", String.class);
        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("concept_name", String.class);

        CrosstabBuilder crosstab = ctab.crosstab()
                .headerCell(DynamicReports.cmp.text("Age Group / Outcome").setStyle(Templates.boldCenteredStyle))
                        .rowGroups(rowGroup.setHeaderStyle(Templates.boldCenteredStyle.setBackgroundColor(Color.GRAY).setBorder(Styles.penDouble())).setShowTotal(false))
                        .columnGroups(columnGroup.setHeaderStyle(Templates.boldCenteredStyle.setBackgroundColor(Color.GRAY).setBorder(Styles.penDouble())).setShowTotal(false))
                        .measures(
                                ctab.measure("Female", "female_count", Integer.class, Calculation.NOTHING),
                                ctab.measure("Male", "male_count", Integer.class, Calculation.NOTHING))
                        .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));

//                                ctab.measure("Female", "female_count", Integer.class, Calculation.SUM),
//                                ctab.measure("Male", "male_count", Integer.class, Calculation.SUM));


        // TODO : pick up columns and query from json config
        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        report
                .setPageFormat(PageType.A0, PageOrientation.LANDSCAPE)
                .title(Components.text("Patient Listing").setHorizontalAlignment(HorizontalAlignment.CENTER))
                .setColumnStyle(textStyle)
                .setReportName("Inpatient Outcome Report")
                .summary(crosstab)
                .setDataSource(" SELECT  distinct answer.concept_full_name as concept_name, " +
                                " reporting_age_group.name AS age_group, " +
                                " IF(inpatient_outcome_obs.age_group_id IS NULL, 0, SUM(IF(inpatient_outcome_obs.gender = 'F', 1, 0))) AS female_count, " +
                                " IF(inpatient_outcome_obs.age_group_id IS NULL, 0, SUM(IF(inpatient_outcome_obs.gender = 'M', 1, 0))) AS male_count, " +
                                " IF(inpatient_outcome_obs.age_group_id IS NULL, 0, COUNT(person_id)) as total_count " +
                                "FROM concept_view as question " +
                                "JOIN concept_answer ON question.concept_id = concept_answer.concept_id " +
                                "JOIN concept_view as answer ON answer.concept_id = concept_answer.answer_concept " +
                                "LEFT OUTER JOIN reporting_age_group ON reporting_age_group.report_group_name = 'Inpatient Discharge Reports' " +
                                "LEFT OUTER JOIN ( " +
                                " SELECT distinct valid_coded_obs_view.concept_id, valid_coded_obs_view.value_coded, observed_age_group.id as age_group_id, person.person_id, person.gender " +
                                " FROM valid_coded_obs_view  " +
                                " LEFT OUTER JOIN person ON valid_coded_obs_view.person_id = person.person_id " +
                                " LEFT OUTER JOIN reporting_age_group as observed_age_group ON observed_age_group.report_group_name = 'Inpatient Discharge Reports' AND " +
                                "   valid_coded_obs_view.obs_datetime BETWEEN (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.min_years YEAR), INTERVAL observed_age_group.min_days DAY))  " +
                                "      AND (DATE_ADD(DATE_ADD(person.birthdate, INTERVAL observed_age_group.max_years YEAR), INTERVAL observed_age_group.max_days DAY))  " +
                                " WHERE valid_coded_obs_view.obs_datetime BETWEEN '2014-09-01' AND '2015-02-12' AND valid_coded_obs_view.concept_full_name = 'Inpatient Outcome' " +
                                ") AS inpatient_outcome_obs ON inpatient_outcome_obs.concept_id = question.concept_id AND inpatient_outcome_obs.value_coded = answer.concept_id  " +
                                "  AND reporting_age_group.id = inpatient_outcome_obs.age_group_id " +
                                "WHERE question.concept_full_name = 'Inpatient Outcome' " +
                                "GROUP BY answer.concept_id, age_group " +
                                "ORDER BY answer.concept_id, reporting_age_group.sort_order;",
                        dataSource.getConnection());

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Inpatient_Outcome.xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        report.toXlsx(outputStream);

        response.flushBuffer();
        outputStream.close();
    }
}
