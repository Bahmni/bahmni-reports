package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericObservationDaoImpl;
import org.bahmni.reports.dao.impl.GenericProgramDaoImpl;
import org.bahmni.reports.dao.impl.GenericVisitDaoImpl;
import org.bahmni.reports.model.*;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
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

        List<String> rowGroups = aggregateReport.getConfig().getRowGroups();
        List<String> columnGroups = aggregateReport.getConfig().getColumnGroups();
        List<String> distinctGroups = aggregateReport.getConfig().getDistinctGroups();
        String reportHeading = null;


        if (rowGroups == null || rowGroups.size() == 0 ||
                columnGroups == null || columnGroups.size() == 0) {
            throw new Exception("Please check rowGroups or columnGroups");
        }

        CrosstabBuilder crosstab = ctab.crosstab();
        CommonComponents.addTo(jasperReport, aggregateReport, pageType);

        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        for (String row : rowGroups) {
            CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup(row, String.class)
                    .setShowTotal(false);
            crosstab.rowGroups(rowGroup);

        }

        for (String column : columnGroups) {
            CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup(column, String.class)
                    .setShowTotal(false);
            crosstab.columnGroups(columnGroup);
        }

        for (String distinctgroup : distinctGroups) {
            crosstab.measures(
                    ctab.measure("", distinctgroup, String.class, Calculation.DISTINCT_COUNT));
        }

        GenericDao genericDao = getReportToAggregate(aggregateReport);
        ResultSet reportData = genericDao != null ? genericDao.getResultSet(connection, startDate, endDate, null) : null;


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
            default:
                return null;

        }
    }

}