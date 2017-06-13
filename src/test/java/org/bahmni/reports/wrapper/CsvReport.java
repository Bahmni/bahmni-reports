package org.bahmni.reports.wrapper;

import com.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvReport {

    private CSVReader csvReader;
    private String reportName;
    private String[] columnHeaders;
    private Map<String, Integer> columnMap;
    private List<String> footers;
    private List<String[]> rows;
    private String footerPattern = "\\d+,,,,, of \\d+,,,";
    private String errorMessage;

    public static CsvReport getReport(String result, String errorMessage) {
        CsvReport report = new CsvReport(result);
        report.setErrorMessage(errorMessage);
        try {
            report.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return report;
    }

    private CsvReport(String result) {
        this.csvReader = new CSVReader(new StringReader(result));
        footers = new ArrayList<>();
        rows = new ArrayList<>();
    }

    private void process() throws IOException {
        String[] row;
        reportName = joinStringArray(csvReader.readNext(), "");
        joinStringArray(csvReader.readNext(), "");
        joinStringArray(csvReader.readNext(), "");
        columnHeaders = csvReader.readNext();
        if (columnHeaders != null)
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

    public int rowsCount() {
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


    public int columnsCount() {
        return columnHeaders.length;
    }


    public String getColumnHeaderAtIndex(int index) {
        return columnHeaders[index];
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
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
