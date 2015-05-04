package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MalariaConfig implements Config{
    private String paraCheck;
    private String psForMp;

    public String getParaCheck() {
        return paraCheck;
    }

    public void setParaCheck(String paraCheck) {
        this.paraCheck = paraCheck;
    }

    public String getPsForMp() {
        return psForMp;
    }

    public void setPsForMp(String psForMp) {
        this.psForMp = psForMp;
    }
}