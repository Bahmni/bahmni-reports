package org.bahmni.reports.report.integrationtests;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;

import java.util.Map;

public class PrimaryKeyFilter implements IColumnFilter{
    private Map<String, String> tablePsuedoKeys = null;

    public PrimaryKeyFilter(Map<String, String> tablePsuedoKeys) {
        this.tablePsuedoKeys = tablePsuedoKeys;
    }

    @Override
    public boolean accept(String tableName, Column column) {
        return column.getColumnName().equalsIgnoreCase(tablePsuedoKeys.get(tableName));
    }
}
