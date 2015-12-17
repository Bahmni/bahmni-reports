package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObsCannedReportTemplateConfig implements Config {
    private String templateName;
    private List<String> patientAttributes;
    private String applyDateRangeFor;
    private List<String> addressAttributes;
    private List<String> conceptNames;
    private List<String> visitIndependentConcept;
    private String enrolledProgram;
    private Boolean showObsOnlyForProgramDuration;
    private String conceptSource;

    public String getTemplateName() {
        return templateName;
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

    public void setAddressAttributes(List<String> addressAttributes) {
        this.addressAttributes = addressAttributes;
    }

    public List<String> getAddressAttributes() {
        return addressAttributes;
    }

    public void setConceptNames(List<String> conceptNames) {
        this.conceptNames = conceptNames;
    }

    public List<String> getConceptNames() {
        return conceptNames;
    }

    public String getEnrolledProgram() {
        return enrolledProgram;
    }

    public void setEnrolledProgram(String enrolledProgram) {
        this.enrolledProgram = enrolledProgram;
    }

    public Boolean getShowObsOnlyForProgramDuration() {
        return showObsOnlyForProgramDuration;
    }

    public void setShowObsOnlyForProgramDuration(Boolean showObsOnlyForProgramDuration) {
        this.showObsOnlyForProgramDuration = showObsOnlyForProgramDuration;
    }

    public List<String> getVisitIndependentConcept() {
        return visitIndependentConcept;
    }

    public void setVisitIndependentConcept(List<String> visitIndependentConcept) {
        this.visitIndependentConcept = visitIndependentConcept;
    }

    public String getConceptSource() {
        return conceptSource;
    }

    public void setConceptSource(String conceptSource) {
        this.conceptSource = conceptSource;
    }
}

