package org.bahmni.reports.report.integrationtests;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;

public class EpisodePatientProgramPrimaryKeyFilter implements IColumnFilter{
    private String psuedoKey = null;
    private String tableName;

    public EpisodePatientProgramPrimaryKeyFilter(String tableName, String psuedoKey) {
        this.tableName = tableName;
        this.psuedoKey = psuedoKey;
    }

    @Override
    public boolean accept(String s, Column column) {
        return tableName.equalsIgnoreCase(s) && column.getColumnName().equalsIgnoreCase(psuedoKey);
    }
}
