package org.bahmni.reports.builder;


import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;

import java.util.Date;

public class ConceptReferenceTermBuilder {
    private final ConceptReferenceTerm conceptReferenceTerm;

    public ConceptReferenceTermBuilder() {
        conceptReferenceTerm = new ConceptReferenceTerm();
    }

    public ConceptReferenceTerm build() {
        return conceptReferenceTerm;
    }

    public ConceptReferenceTermBuilder withName(String conceptName) {
        this.conceptReferenceTerm.setName(conceptName);
        return this;
    }


    public ConceptReferenceTermBuilder withCode(String name) {
        this.conceptReferenceTerm.setCode(name);
        return this;
    }

    public ConceptReferenceTermBuilder withConceptSource(ConceptSource conceptSource)
    {   this.conceptReferenceTerm.setConceptSource(conceptSource);
        return this;
    }

    public ConceptReferenceTermBuilder withDateCreated(Date dateCreated) {
        this.conceptReferenceTerm.setDateCreated(dateCreated);
        return this;
    }
}
