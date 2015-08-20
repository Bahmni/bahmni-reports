package org.bahmni.reports.model;

import java.util.List;

public class VisitReportConfig implements Config {

    private List<String> personAttributes;
    private List<String> visitAttributes;

    public List<String> getPersonAttributes() {
        return personAttributes;
    }

    public List<String> getVisitAttributes() {
        return visitAttributes;
    }

}