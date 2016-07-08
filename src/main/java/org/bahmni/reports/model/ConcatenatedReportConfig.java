package org.bahmni.reports.model;

import java.util.List;


public class ConcatenatedReportConfig implements Config {

    private List<Report> reports;

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
}
