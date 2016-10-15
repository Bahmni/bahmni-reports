package org.bahmni.reports.builder;


import org.openmrs.ConceptSource;

import java.util.Date;

public class ConceptReferenceSourceBuilder {
    private final ConceptSource conceptSource;

    public ConceptReferenceSourceBuilder() {
        conceptSource = new ConceptSource();
    }

    public ConceptSource build() {
        return conceptSource;
    }

    public ConceptReferenceSourceBuilder withName(String conceptName) {
        this.conceptSource.setName(conceptName);
        return this;
    }


    public ConceptReferenceSourceBuilder withDescription(String description) {
        this.conceptSource.setDescription(description);
        return this;
    }

    public ConceptReferenceSourceBuilder withDateCreated(Date dateCreated) {
        this.conceptSource.setDateCreated(dateCreated);
        return this;
    }

    public ConceptReferenceSourceBuilder withRetired(Boolean retired){
        this.conceptSource.setRetired(retired);
        return this;
    }
}
