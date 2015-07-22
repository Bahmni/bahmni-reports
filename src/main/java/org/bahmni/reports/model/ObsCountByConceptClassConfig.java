package org.bahmni.reports.model;

import java.util.List;

public class ObsCountByConceptClassConfig implements Config {

    private List<String> conceptClassNames;

    public List<String> getConceptClassNames() {
        return conceptClassNames;
    }

    public void setConceptClassNames(List<String> conceptClassNames) {
        this.conceptClassNames = conceptClassNames;
    }
}
