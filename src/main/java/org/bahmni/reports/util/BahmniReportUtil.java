package org.bahmni.reports.util;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.DRReport;
import net.sf.dynamicreports.report.base.column.DRColumn;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.GenericReportsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.web.ReportHeader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.*;

import static net.sf.dynamicreports.report.builder.DynamicReports.report;

public class BahmniReportUtil {

    public static BahmniReportBuilder build(Report report, Connection connection,
                                            String startDate, String endDate,
                                            List<AutoCloseable> resources, PageType pageType,
                                            BahmniReportsProperties bahmniReportsProperties) throws Exception {
        BaseReportTemplate reportTemplate = report.getTemplate(bahmniReportsProperties);
        JasperReportBuilder reportBuilder = report();
        reportBuilder = new ReportHeader().add(reportBuilder, report.getName(), startDate, endDate);
        BahmniReportBuilder build = reportTemplate.build(connection, reportBuilder, report, startDate, endDate, resources, pageType);
        excludeColumns(report.getConfig(), reportBuilder);
        orderColumns(report.getConfig(), reportBuilder);
        return build;
    }

    private static boolean contains(List<String> columns, String searchColumn) {
        for (String column : columns) {
            if (column.equalsIgnoreCase(searchColumn)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> removeDuplicatesFrom(List<String> duplicates) {
        List<String> disticts = new ArrayList<String>();
        if (duplicates != null && !duplicates.isEmpty())
            for (String word : duplicates) {
                if (!disticts.contains(word)) {
                    disticts.add(word);
                }
            }
        return disticts;
    }

    private static void orderColumns(Config config, JasperReportBuilder reportBuilder){
        if (config instanceof GenericReportsConfig) {
            List<String> allColumns = ((GenericReportsConfig) config).getPreferredColumns();
            List<String> columns = removeDuplicatesFrom(allColumns);
            List<String> excludeColumns = ((GenericReportsConfig) config).getExcludeColumns();
            if (columns != null && columns.size() > 0) {
                DRReport drReport = reportBuilder.getReport();
                List<DRColumn<?>> jasperReportColumns = drReport.getColumns();
                List<DRColumn<?>> orderedColumns = new ArrayList<>(jasperReportColumns.size());
                List<String> reportColumns = getColumnsList(jasperReportColumns);
                for (int index = 0; index < columns.size(); index++) {
                    if ((excludeColumns != null && contains(excludeColumns, columns.get(index))) || !reportColumns.contains(columns.get(index).toLowerCase())) {
                        columns.remove(index);
                        index--;
                    }
                }
                for (String column : columns) {
                    orderedColumns.add(columns.indexOf(column),jasperReportColumns.get(reportColumns.indexOf(column.toLowerCase())));
                }
                jasperReportColumns.removeAll(orderedColumns);
                orderedColumns.addAll(orderedColumns.size(), jasperReportColumns);
                drReport.setColumns(orderedColumns);
            }
        }
    }

    private static List<String> getColumnsList(List<DRColumn<?>> jasperReportColumns) {
        List<String> reportColumns = new ArrayList<>();
        for (DRColumn col :jasperReportColumns) {
            reportColumns.add(col.getName().toLowerCase());
        }
        return reportColumns;
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
            if (! isExcluded(excludeColumnsList, column)) {
                columnsToAdd.add(column);
            }
        }
        report.setColumns(columnsToAdd);
    }

    private static boolean isExcluded(List<String> excludeColumnsList, final DRColumn<?> column) {

        int countMatches = CollectionUtils.countMatches(excludeColumnsList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return column.getName().equalsIgnoreCase(String.valueOf(o));
            }
        });
        return countMatches > 0 ;
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
