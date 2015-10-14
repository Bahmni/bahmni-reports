package org.bahmni.reports.wrapper;

import com.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report {

    private CSVReader csvReader;
    private String reportName;
    private String dateHeader;
    private String[] columnHeaders;
    private Map<String, Integer> columnMap;
    private List<String> footers;
    private List<String[]> rows;
    private String footerPattern = "\\d+,,,,, of \\d+,,,";

    public static Report getReport(String result) {
        Report report = new Report(result);
        try {
            report.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return report;
    }

    private Report(String result) {
        this.csvReader = new CSVReader(new StringReader(result));
        footers = new ArrayList<>();
        rows = new ArrayList<>();
    }

    private void process() throws IOException {
        String[] row;
        reportName = joinStringArray(csvReader.readNext(), "");
        dateHeader = joinStringArray(csvReader.readNext(), "");
        columnHeaders = csvReader.readNext();
        columnMap = buildIndexMap(columnHeaders);
        boolean pageStart = false;
        while ((row = csvReader.readNext()) != null) {
            if (pageStart) {
                csvReader.readNext();
                pageStart = false;
            } else {
                if (joinStringArray(row, ",").matches(footerPattern)) {
                    footers.add(joinStringArray(row, ""));
                    pageStart = true;
                } else
                    rows.add(row);
            }
        }

    }

    public int numberOfRows() {
        return rows.size();
    }

    public String getReportName() {
        return reportName;
    }

    public String[] getRow(int rowNumber) {
        return rows.get(rowNumber - 1);
    }

    public String getRowAsString(int rowNumber, String delimiter) {
        return joinStringArray(getRow(rowNumber), delimiter).trim();
    }

    public String getColumnValueInRow(int rowNumber, String columnName) {
        return getRow(rowNumber)[columnMap.get(columnName)];
    }

    public String[] getRowHavingColumnValue(String columnName, String value) {
        int indexOfColumn = columnMap.get(columnName);
        for (String[] row : rows) {
            if (row[indexOfColumn].equals(value))
                return row;
        }
        return null;
    }

    public int getNumberOfColumns() {
        return columnHeaders.length;
    }

    public String[] getColumnHeaders() {
        return columnHeaders;
    }

    private HashMap<String, Integer> buildIndexMap(String[] array) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        for (int index = 0; index < array.length; index++) {
            hashMap.put(columnHeaders[index], index);
        }
        return hashMap;
    }

    private static String joinStringArray(String[] array, String delimiter) {
        return StringUtils.join(array, delimiter);
    }
}
