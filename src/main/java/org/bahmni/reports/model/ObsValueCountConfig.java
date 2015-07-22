package org.bahmni.reports.model;

import java.util.List;

public class ObsValueCountConfig implements Config {

    private List<String> conceptNames;

    public List<String> getConceptNames() {
        return conceptNames;
    }

    public void setConceptNames(List<String> conceptNames) {
        this.conceptNames = conceptNames;
    }
}
