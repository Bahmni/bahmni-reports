package org.bahmni.reports.model;

public class ObsCountByConceptClassConfig implements Config {

    private String conceptClassNames;

    public String getConceptClassNames() {
        return conceptClassNames;
    }

    public void setConceptClassNames(String conceptClassNames) {
        this.conceptClassNames = conceptClassNames;
    }
}
