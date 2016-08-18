package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.expression.AddExpression;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericLabOrderDaoImpl;
import org.bahmni.reports.dao.impl.GenericObservationDaoImpl;
import org.bahmni.reports.dao.impl.GenericProgramDaoImpl;
import org.bahmni.reports.dao.impl.GenericVisitDaoImpl;
import org.bahmni.reports.model.AggregationReportConfig;
import org.bahmni.reports.model.GenericReportsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static org.bahmni.reports.model.Constants.*;

@UsingDatasource("openmrs")
public class AggregationReportTemplate extends BaseReportTemplate<AggregationReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public AggregationReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<AggregationReportConfig> aggregateReport,
                                     String startDate, String endDate, List<AutoCloseable> resources,
                                     PageType pageType) throws Exception {

        AggregationReportConfig config = aggregateReport.getConfig();
        List<String> rowGroups = config.getRowGroups();
        List<String> columnGroups = config.getColumnGroups();
        List<String> distinctGroups = config.getDistinctGroups();
        String reportHeading = null;


        if (rowGroups == null || rowGroups.size() == 0) {
            throw new Exception("You have not configured rowGroups.");
        }

        CrosstabBuilder crosstab = ctab.crosstab();
        CommonComponents.addTo(jasperReport, aggregateReport, pageType);

        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        addRowGroup(config, rowGroups, crosstab);

        addColumnGroup(config, columnGroups, crosstab);

        if (distinctGroups.size() == 1) {
            crosstab.measures(
                    ctab.measure("", distinctGroups.get(0), Object.class, Calculation.DISTINCT_COUNT).setTitleStyle(stl.style().setFontSize(0)));
        } else {
            for (String distinctgroup : distinctGroups) {
                crosstab.measures(
                        ctab.measure(distinctgroup, distinctgroup, Object.class, Calculation.DISTINCT_COUNT));
            }
        }

        GenericDao genericDao = getReportToAggregate(aggregateReport);
        ResultSet reportData = genericDao != null ? genericDao.getResultSet(connection, startDate, endDate, new ArrayList<String>()) : null;


        reportHeading = reportData != null ? "Aggregation report for " + aggregateReport.getName() :
                "Data is not available for this Date Range";

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setFontSize(20);
        jasperReport.addTitle(cmp.horizontalList()
                .add(cmp.text(reportHeading)
                        .setStyle(Templates.boldStyle)
                        .setStyle(textStyle)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER))
                .newRow()
                .add(cmp.verticalGap(10))
        );

        JasperReportBuilder jasperReportBuilder = reportData != null ? jasperReport.setDataSource(reportData).summary(crosstab) : jasperReport;
        return new BahmniReportBuilder(jasperReportBuilder);
    }

    private void addRowGroup(AggregationReportConfig config, List<String> rowGroups, CrosstabBuilder crosstab) {
        for (String row : rowGroups) {
            if (applyAgeGroup(row, config)) {
                createAgeGroupRows(row, config, crosstab);
            } else {
                CrosstabRowGroupBuilder rowGroup = ctab.rowGroup(row, Object.class)
                        .setShowTotal(config.getShowTotalRow());
                crosstab.rowGroups(rowGroup);
            }
        }
    }

    private void addColumnGroup(AggregationReportConfig config, List<String> columnGroups, CrosstabBuilder crosstab) {
        if (CollectionUtils.isEmpty(columnGroups)) {
            crosstab.columnGroups(ctab.columnGroup(new AddExpression())
                    .setShowTotal(config.getShowTotalColumn()).setHeaderStyle(stl.style(Templates.columnStyle)));
            return;
        }
        for (String column : columnGroups) {
            if (applyAgeGroup(column, config)) {
                createAgeGroupColumns(column, config, crosstab);
            } else {
                CrosstabColumnGroupBuilder columnGroup = ctab.columnGroup(column, Object.class)
                        .setShowTotal(config.getShowTotalColumn());
                crosstab.columnGroups(columnGroup);
            }
        }
    }

    private void createAgeGroupColumns(String column, AggregationReportConfig config, CrosstabBuilder crosstab) {
        CrosstabColumnGroupBuilder<Integer> sortOrderGroup = ctab.columnGroup("Age Group Order", Integer.class)
                .setShowTotal(config.getShowTotalColumn())
                .setHeaderStyle(stl.style().setFontSize(0))
                .setOrderType(OrderType.ASCENDING);
        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup(column, String.class)
                .setShowTotal(false);
        crosstab.columnGroups(sortOrderGroup, columnGroup);
    }

    private void createAgeGroupRows(String row, AggregationReportConfig config, CrosstabBuilder crosstab) {
        CrosstabRowGroupBuilder<Integer> sortOrderGroup = ctab.rowGroup("Age Group Order", Integer.class)
                .setShowTotal(config.getShowTotalRow())
                .setHeaderWidth(0)
                .setOrderType(OrderType.ASCENDING);
        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup(row, String.class)
                .setShowTotal(false);
        crosstab.rowGroups(sortOrderGroup, rowGroup);
    }

    private boolean applyAgeGroup(String row, AggregationReportConfig config) {
        if (config.getReport().getConfig() == null) {
            return false;
        }
        GenericReportsConfig genericReportsConfig = (GenericReportsConfig) config.getReport().getConfig();
        return row.equals(genericReportsConfig.getAgeGroupName());
    }


    private GenericDao getReportToAggregate(Report<AggregationReportConfig> aggregateReport) {
        Report report = aggregateReport.getConfig().getReport();
        report.setHttpClient(aggregateReport.getHttpClient());
        switch (aggregateReport.getConfig().getReport().getType()) {
            case OBSERVAIONS:
                return new GenericObservationDaoImpl(report, bahmniReportsProperties);
            case VISITS:
                return new GenericVisitDaoImpl(report);
            case PROGRAMS:
                return new GenericProgramDaoImpl(report);
            case LABORDERS:
                return new GenericLabOrderDaoImpl(report, bahmniReportsProperties);
            default:
                return null;

        }
    }

}