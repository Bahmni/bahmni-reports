package org.bahmni.reports.icd10.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ICDRule {
    public String mapPriority;
    public String mapGroup;
    public String mapRule;
    public String mapTarget;

    @Override
    public String toString() {
        return this.mapGroup + " " + this.mapPriority + " " + this.mapRule;
    }

    public String getMapPriority() {
        return mapPriority;
    }

    public void setMapPriority(String mapPriority) {
        this.mapPriority = mapPriority;
    }

    public String getMapGroup() {
        return mapGroup;
    }

    public void setMapGroup(String mapGroup) {
        this.mapGroup = mapGroup;
    }

    public String getMapRule() {
        return mapRule;
    }

    public void setMapRule(String mapRule) {
        this.mapRule = mapRule;
    }

    public String getMapTarget() {
        return mapTarget;
    }

    public void setMapTarget(String mapTarget) {
        this.mapTarget = mapTarget;
    }
}