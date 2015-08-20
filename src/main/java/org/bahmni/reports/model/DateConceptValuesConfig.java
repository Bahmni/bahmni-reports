package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DateConceptValuesConfig implements Config {

    private List<String> conceptNames;
    private String templateName;

    public List<String> getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(List<String> conceptNames) {
        this.conceptNames = conceptNames;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

}