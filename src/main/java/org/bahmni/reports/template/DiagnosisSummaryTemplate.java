package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class DiagnosisSummaryTemplate extends BaseReportTemplate<DiagnosisReportConfig> {

    private WhereClause observationsWhereClause = new WhereClause();

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<DiagnosisReportConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        DiagnosisReportConfig reportSpecificConfig = report.getConfig();

        CrosstabBuilder crosstab = ctab.crosstab()
                .rowGroups(createRowGroupBuilder(reportSpecificConfig))
                .columnGroups(createColumnGroupBuilder(reportSpecificConfig))
                .measures(
                        ctab.measure(null, "diagnosis_count", Integer.class, Calculation.SUM)
                )
                .setCellStyle(Templates.columnStyle.setBorder(Styles.pen()));

        handleVisitCharacteristics(jasperReport, reportSpecificConfig);

        addDateParameters(startDate, endDate, reportSpecificConfig);
        addVisitTypeParameters(reportSpecificConfig);

        jasperReport.setColumnStyle(Templates.columnStyle)
                .summary(crosstab)
                .setDataSource(getSqlString(reportSpecificConfig), connection);
        return new BahmniReportBuilder(jasperReport);
    }

    private void addDateParameters(String startDate, String endDate, DiagnosisReportConfig reportSpecificConfig) {
        if (reportSpecificConfig.retrieveBasedOnDiagnosisDatetime()) {
            observationsWhereClause.and("cast(diagnosis.obs_datetime AS date) BETWEEN '" + startDate + "' AND '" + endDate + "'");
        }

        if (reportSpecificConfig.retrieveBasedOnVisitStopDate()) {
            observationsWhereClause.and("cast(v.date_stopped AS date) BETWEEN '" + startDate + "' AND '" + endDate + "'");
        }
    }

    private void addVisitTypeParameters(DiagnosisReportConfig reportSpecificConfig) {
        if (reportSpecificConfig.visitTypesPresent()) {
            observationsWhereClause.and("vt.name in ( " + SqlUtil.toCommaSeparatedSqlString(reportSpecificConfig.getVisitTypes()) + " )");
        }
    }

    private void handleVisitCharacteristics(JasperReportBuilder jasperReport, DiagnosisReportConfig reportSpecificConfig) {
        if (reportSpecificConfig.retrieveOnlyOpenVisits()) {
            StringBuilder subHeader = new StringBuilder();
            subHeader.append("For currently open visits");
            observationsWhereClause.and("v.date_stopped is null");

            jasperReport.addTitle(cmp.horizontalList()
                            .add(cmp.text(subHeader.toString())
                                    .setStyle(Templates.boldStyle)
                                    .setHorizontalAlignment(HorizontalAlignment.LEFT))
                            .newRow()
                            .add(cmp.verticalGap(10))
            );
        }

        if (reportSpecificConfig.retrieveOnlyClosedVisits()) {
            observationsWhereClause.and("v.date_stopped is not null");
        }
    }

    private CrosstabColumnGroupBuilder<String>[] createColumnGroupBuilder(DiagnosisReportConfig reportSpecificConfig) {
        List<String> columnsGroupBy = reportSpecificConfig.getColumnsGroupBy(true);
        CrosstabColumnGroupBuilder<String>[] columnGroups = new CrosstabColumnGroupBuilder[columnsGroupBy.size()];
        for (int i = 0; i < columnsGroupBy.size(); i++) {
            columnGroups[i] = ctab.columnGroup(columnsGroupBy.get(i), String.class)
                    .setShowTotal(true);
        }
        return columnGroups;
    }

    private CrosstabRowGroupBuilder<String>[] createRowGroupBuilder(DiagnosisReportConfig reportSpecificConfig) {
        List<String> rowsGroupBy = reportSpecificConfig.getRowsGroupBy(true);
        CrosstabRowGroupBuilder<String>[] rowGroups = new CrosstabRowGroupBuilder[rowsGroupBy.size()];
        for (int i = 0; i < rowsGroupBy.size(); i++) {
            rowGroups[i] = ctab.rowGroup(rowsGroupBy.get(i), String.class)
                    .setShowTotal(true);
        }
        return rowGroups;
    }

    private String getSqlString(DiagnosisReportConfig reportConfig) {
        String sql = getFileContent("sql/diagnosisSummary.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("observationsWhereClause", observationsWhereClause);
        sqlTemplate.add("ageGroupName", reportConfig.getAgeGroupName(true));
        sqlTemplate.add("conceptName", reportConfig.getConcept());


        String locationTagNames = SqlUtil.toCommaSeparatedSqlString(reportConfig.getLocationTagNames());
        String countOnlyTaggedLocationsJoin = String.format("INNER JOIN " +
                "(SELECT DISTINCT location_id " +
                " FROM location_tag_map INNER JOIN location_tag ON location_tag_map.location_tag_id = location_tag.location_tag_id " +
                " AND location_tag.name IN (%s)) locations ON locations.location_id = e.location_id", locationTagNames);

        if (StringUtils.isNotBlank(locationTagNames)) {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", countOnlyTaggedLocationsJoin);
        } else {
            sqlTemplate.add("countOnlyTaggedLocationsJoin", "");
        }
        return sqlTemplate.render();
    }

    private class WhereClause {
        public static final String SPACE = " ";
        public static final String AND = "AND";
        private static final String WHERE = "WHERE";
        private StringBuffer clauses = new StringBuffer();
        private boolean hasClauses;

        public WhereClause and(String condition) {
            and();
            append(condition);
            return this;
        }

        private StringBuffer append(String condition) {
            return clauses.append(SPACE).append(condition).append(SPACE);
        }

        private StringBuffer and() {
            if (!hasClauses) {
                hasClauses = true;
                clauses.append(SPACE).append(WHERE).append(SPACE);
            } else {
                clauses.append(SPACE).append(AND).append(SPACE);
            }
            return clauses;
        }

        @Override
        public String toString() {
            return clauses.toString();
        }
    }
}