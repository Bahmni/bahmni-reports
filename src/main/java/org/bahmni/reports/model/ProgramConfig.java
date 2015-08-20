package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramConfig implements Config {
    private String programName;

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getProgramName() {
        return programName;
    }
}
