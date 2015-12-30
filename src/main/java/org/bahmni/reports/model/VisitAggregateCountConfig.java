package org.bahmni.reports.model;

import java.util.List;

public class VisitAggregateCountConfig implements Config {

    private String visitTypes;
    private List<String> locationTagNames;

    public List<String> getLocationTagNames() {
        return locationTagNames;
    }

    public void setLocationTagNames(List<String> locationTagNames) {
        this.locationTagNames = locationTagNames;
    }

    public String getVisitTypes() {
        return visitTypes;
    }
}