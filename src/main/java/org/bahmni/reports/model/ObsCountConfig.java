package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObsCountConfig implements Config {

    private List<String> conceptNames;
    private List<String> locationTagNames;
    private String ageGroupName;
    private List<String> visitTypes;
    //Dont give boolean here... Default value of boolean is false, and which contradicts with requirement (Default true, if no config
    // element found),
    private String countOnlyClosedVisits;
    private String countOncePerPatient;

    public List<String> getConceptNames() {
        return conceptNames;
    }

    public List<String> getLocationTagNames() { return locationTagNames; }

    public void setConceptNames(List<String> conceptNames) {
        this.conceptNames = conceptNames;
    }

    public void setLocationTagNames(List<String> locationTagNames) {
        this.locationTagNames = locationTagNames;
    }

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public List<String> getVisitTypes() {
        return visitTypes;
    }

    public void setVisitTypes(List<String> visitTypes) {
        this.visitTypes = visitTypes;
    }

    public String getCountOnlyClosedVisits() {
        return countOnlyClosedVisits;
    }


    public String getCountOncePerPatient() {
        return countOncePerPatient;
    }

    public void setCountOnlyClosedVisits(String countOnlyClosedVisits) {
        this.countOnlyClosedVisits = countOnlyClosedVisits;
    }

    public void setCountOncePerPatient(String countOncePerPatient) {
        this.countOncePerPatient = countOncePerPatient;
    }


}
