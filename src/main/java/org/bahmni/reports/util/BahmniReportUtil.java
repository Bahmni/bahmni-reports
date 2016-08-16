package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.DRReport;
import net.sf.dynamicreports.report.base.DRSort;
import net.sf.dynamicreports.report.base.column.DRColumn;
import net.sf.dynamicreports.report.constant.OrderType;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.definition.expression.DRIExpression;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.GenericReportsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.web.ReportHeader;
import org.bahmni.webclients.HttpClient;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.report;

public class BahmniReportUtil {

    public static BahmniReportBuilder build(Report report, HttpClient httpClient, Connection connection,
                                            String startDate, String endDate,
                                            List<AutoCloseable> resources, PageType pageType,
                                            BahmniReportsProperties bahmniReportsProperties) throws Exception {
        report.setHttpClient(httpClient);
        BaseReportTemplate reportTemplate = report.getTemplate(bahmniReportsProperties);
        JasperReportBuilder reportBuilder = report();
        reportBuilder = new ReportHeader().add(reportBuilder, report.getName(), startDate, endDate);
        BahmniReportBuilder build = reportTemplate.build(connection, reportBuilder, report, startDate, endDate, resources, pageType);
        excludeColumns(report.getConfig(), reportBuilder);
        sortColumns(report.getConfig(), reportBuilder);
        return build;
    }

    private static void sortColumns(Config config, JasperReportBuilder reportBuilder) {
        if (config instanceof GenericReportsConfig) {
            GenericReportsConfig genericReportsConfig = (GenericReportsConfig) config;
            List<String> sortColumnsList = genericReportsConfig.getSortColumns();
            DRReport report = reportBuilder.getReport();
            for (DRColumn<?> column : report.getColumns()) {
                sortIfConfigured(sortColumnsList, report, column);
            }
        }
    }

    private static void sortIfConfigured(List<String> sortColumnsList, DRReport report, DRColumn<?> column) {
        if (contains(sortColumnsList, column)) {
            DRSort drSort = new DRSort();
            drSort.setExpression((DRIExpression<?>) column);
            drSort.setOrderType(OrderType.ASCENDING);
            report.addSort(drSort);
        }
    }

    private static void excludeColumns(Config config, JasperReportBuilder reportBuilder) {
        if (config instanceof GenericReportsConfig) {
            GenericReportsConfig genericReportsConfig = (GenericReportsConfig) config;
            List<String> excludeColumnsList = genericReportsConfig.getExcludeColumns();
            if (CollectionUtils.isNotEmpty(excludeColumnsList)) {
                filterColumns(reportBuilder.getReport(), excludeColumnsList);
            }
            if (reportBuilder.getReport().getColumns().size() == 0) {
                throw new IllegalArgumentException("You have excluded all columns.");
            }
        }
    }

    private static void filterColumns(DRReport report, List<String> excludeColumnsList) {
        List<DRColumn<?>> columns = report.getColumns();
        List<DRColumn<?>> columnsToAdd = new ArrayList<>();
        for (final DRColumn<?> column : columns) {
            if (!contains(excludeColumnsList, column)) {
                columnsToAdd.add(column);
            }
        }
        report.setColumns(columnsToAdd);
    }

    private static boolean contains(List<String> columnsList, final DRColumn<?> column) {

        int countMatches = CollectionUtils.countMatches(columnsList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return column.getName().equalsIgnoreCase(String.valueOf(o));
            }
        });
        return countMatches > 0 ;
    }
}
