package org.bahmni.reports.controller;

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
import org.apache.log4j.Logger;
import org.bahmni.reports.Templates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

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
        } finally {
            response.flushBuffer();
            response.getOutputStream().close();
        }
    }

    private void writeExcelToResponse(HttpServletResponse response) throws IOException, DRException, SQLException {
        // TODO : pick up columns, row/column for crosstab and query from json config
        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("age_group", String.class)
//                                                    .setHeaderStyle(Templates.boldCenteredStyle.setBackgroundColor(Color.GRAY))
                                                    .setShowTotal(false);

        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("concept_name", String.class)
//                                                    .setHeaderStyle(Templates.boldCenteredStyle.setBackgroundColor(Color.GRAY))
                                                    .setShowTotal(false);

        CrosstabBuilder crosstab = ctab.crosstab()
                        .headerCell(DynamicReports.cmp.text("Age Group / Outcome"))
                        .rowGroups(rowGroup)
                        .columnGroups(columnGroup)
                        .measures(ctab.measure("Female", "female_count", Integer.class, Calculation.NOTHING),
                                ctab.measure("Male", "male_count", Integer.class, Calculation.NOTHING),
                                ctab.measure("Total", "total_count", Integer.class, Calculation.NOTHING)
                                )
                        .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));
//                                ctab.measure("Female", "female_count", Integer.class, Calculation.SUM),
//                                ctab.measure("Male", "male_count", Integer.class, Calculation.SUM));


        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());

        JasperReportBuilder report = report();
        report.setPageFormat(PageType.A3, PageOrientation.LANDSCAPE)
                .title(cmp.text(getReportName()))
                .setColumnStyle(textStyle)
                .setTemplate(Templates.reportTemplate)
                .setReportName(getReportName())
                .summary(crosstab)
                .pageFooter(Templates.footerComponent)
                .setDataSource(getSql(), dataSource.getConnection());

        report.toXlsx(getExcelServletOutputStream(response));
//        report.toHtml(response.getOutputStream());
    }

    private ServletOutputStream getExcelServletOutputStream(HttpServletResponse response) throws IOException {
            response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Inpatient_Outcome.xlsx");
        return response.getOutputStream();
    }

    private static String getReportName() {
        return "Inpatient Outcome Report";
    }

    private static String getSql() {
        return " SELECT  distinct answer.concept_full_name as concept_name, " +
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
                        "ORDER BY answer.concept_id, reporting_age_group.sort_order;";
    }
}