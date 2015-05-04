package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DateConceptValuesConfig implements Config{
    private String conceptNames;
    private String templateName;


    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(String conceptNames) {
        this.conceptNames = conceptNames;
    }
}