package org.bahmni.reports.extensions.sample;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.bahmni.reports.extensions.ResultSetExtension;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.columnStyle;

public class SampleResultSetExtension implements ResultSetExtension {
    public static final String SAMPLE_COLUMN_NAME = "Sample Column";

    public void enrich(Collection<Map<String, ?>> collection, JasperReportBuilder jasperReport) throws SQLException {
        for (Map<String, ?> rowMap : collection) {
            enrichRow(rowMap, SAMPLE_COLUMN_NAME, String.valueOf(new Random().nextInt()));
        }
        jasperReport.addColumn(col.column(SAMPLE_COLUMN_NAME, SAMPLE_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
    }
}