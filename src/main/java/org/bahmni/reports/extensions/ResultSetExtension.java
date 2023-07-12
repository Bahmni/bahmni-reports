package org.bahmni.reports.extensions;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;

import java.util.Collection;
import java.util.Map;

public interface ResultSetExtension {
    void enrich(Collection<Map<String, ?>> collection, JasperReportBuilder jasperReport) throws Exception;

    default <T> void enrichRow(Map<String, T> rowMap, String columnName, String columnValue) {
        rowMap.put(columnName, (T) columnValue);
    }
}
