package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObsCountConfig implements Config {

    private String conceptNames;
    private String ageGroupName;
    private String visitTypes;
    //Dont give boolean here... Default value of boolean is false, and which contradicts with requirement (Default true, if no config element found),
    private String countOnlyClosedVisits;


    public String getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(String conceptNames) {
        this.conceptNames = conceptNames;
    }

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public String getVisitTypes() {
        return visitTypes;
    }

    public void setVisitTypes(String visitTypes) {
        this.visitTypes = visitTypes;
    }

    public String getCountOnlyClosedVisits() {
        return countOnlyClosedVisits;
    }

    public void setCountOnlyClosedVisits(String countOnlyClosedVisits) {
        this.countOnlyClosedVisits = countOnlyClosedVisits;
    }
}
