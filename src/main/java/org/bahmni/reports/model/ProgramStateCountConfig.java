package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramStateCountConfig implements Config{
    private String programName;

    public void setProgramName(String programName){
        this.programName = programName;
    }

    public String getProgramName(){
        return programName;
    }
}
