package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObsTemplateConfig implements Config {
    private String templateName;
    private List<String> patientAttributes;
    private List<String> locationTagNames;
    private String applyDateRangeFor;
    private String conceptSource;

    public String getTemplateName() {
        return templateName;
    }

    public List<String> getLocationTagNames() {
        return locationTagNames;
    }

    public void setLocationTagNames(List<String> locationTagNames) {
        this.locationTagNames = locationTagNames;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public List<String> getPatientAttributes() {
        return patientAttributes;
    }

    public void setPatientAttributes(List<String> patientAttributes) {
        this.patientAttributes = patientAttributes;
    }

    public String getApplyDateRangeFor() {
        return applyDateRangeFor;
    }

    public void setApplyDateRangeFor(String applyDateRangeFor) {
        this.applyDateRangeFor = applyDateRangeFor;
    }

    public String getConceptSource() {
        return conceptSource;
    }

    public void setConceptSource(String conceptSource) {
        this.conceptSource = conceptSource;
    }
}

