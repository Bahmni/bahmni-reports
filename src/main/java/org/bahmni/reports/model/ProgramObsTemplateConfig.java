package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramObsTemplateConfig extends ObsTemplateConfig {
    private List<String> programAttributes;
    private List<String> programNames;

    public List<String> getProgramAttributes() {
        return programAttributes;
    }

    public void setProgramAttributes(List<String> programAttributes) {
        this.programAttributes = programAttributes;
    }

    public List<String> getProgramNames() {
        return programNames;
    }

    public void setProgramNames(List<String> programNames) {
        this.programNames = programNames;
    }
}
