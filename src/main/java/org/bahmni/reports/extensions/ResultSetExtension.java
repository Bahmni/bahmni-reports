package org.bahmni.reports.extensions;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.apache.commons.lang3.StringUtils;
import java.util.Collection;
import java.util.Map;

public interface ResultSetExtension {
    static final String EMPTY_STRING = "";

    void enrich(Collection<Map<String, ?>> collection, JasperReportBuilder jasperReport) throws Exception;

    default <T> void enrichRow(Map<String, T> rowMap, String columnName, String columnValue) {
        if(StringUtils.isBlank(columnValue)){
            columnValue = EMPTY_STRING;
        }
        rowMap.put(columnName, (T) columnValue);
    }
}
