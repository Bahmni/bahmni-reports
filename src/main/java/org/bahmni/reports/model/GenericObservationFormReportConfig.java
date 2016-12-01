package org.bahmni.reports.model;

import java.util.List;

public class GenericObservationFormReportConfig extends GenericObservationReportConfig implements Config {

    private List<String> programAttributes;
    private List<String> formNamesToFilter;
    public List<String> getProgramAttributes() {
        return programAttributes;
    }

    public void setProgramAttributes(List<String> programAttributes) {
        this.programAttributes = programAttributes;
    }

    public List<String> getFormNamesToFilter() {
        return formNamesToFilter;
    }

    public void setFormNamesToFilter(List<String> formNamesToFilter) {
        this.formNamesToFilter = formNamesToFilter;
    }
}