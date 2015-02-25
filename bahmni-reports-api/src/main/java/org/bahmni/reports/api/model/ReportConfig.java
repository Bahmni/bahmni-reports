package org.bahmni.reports.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportConfig {
    private String title;
    private String name;
    private String ageGroupName;
    private String conceptName;
    private String templateName;
    private String startDate;
    private String stopDate;

    public ReportConfig(String title, String name, String ageGroupName, String conceptName, String templateName, String startDate, String stopDate) {
        this.title = title;
        this.name = name;
        this.ageGroupName = ageGroupName;
        this.conceptName = conceptName;
        this.templateName = templateName;
        this.startDate = startDate;
        this.stopDate = stopDate;
    }

    public ReportConfig() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAgeGroupName() {
        return ageGroupName;
    }

    public void setAgeGroupName(String ageGroupName) {
        this.ageGroupName = ageGroupName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStopDate() {
        return stopDate;
    }

    public void setStopDate(String stopDate) {
        this.stopDate = stopDate;
    }
}
