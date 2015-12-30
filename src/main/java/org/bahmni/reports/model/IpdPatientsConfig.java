package org.bahmni.reports.model;

import java.util.List;

public class IpdPatientsConfig implements Config {

    private List<String> patientAttributes;
    private List<String> conceptNames;
    private List<String> addressAttributes;
    private List<String> locationTagNames;
    private String filterBy;

    public List<String> getLocationTagNames() {
        return locationTagNames;
    }

    public void setLocationTagNames(List<String> locationTagNames) {
        this.locationTagNames = locationTagNames;
    }

    public List<String> getPatientAttributes() {
        return patientAttributes;
    }

    public List<String> getAddressAttributes() {
        return addressAttributes;
    }

    public List<String> getConceptNames() {
        return conceptNames;
    }

    public String getFilterBy() {
        return filterBy;
    }
}