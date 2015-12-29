package org.bahmni.reports.builder;


import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;

import java.util.Date;

public class ConceptReferenceMapBuilder {

    private ConceptMap conceptMap;
    public ConceptReferenceMapBuilder() {
        conceptMap = new ConceptMap();
    }

    public  ConceptReferenceMapBuilder withConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm){
        this.conceptMap.setConceptReferenceTerm(conceptReferenceTerm);
        return this;
    }

    public ConceptReferenceMapBuilder withConcept(Concept concept){
        this.conceptMap.setConcept(concept);
        return this;
    }

    public ConceptReferenceMapBuilder withDateCreated(Date dateCreated){
        this.conceptMap.setDateCreated(dateCreated);
        return this;
    }

    public ConceptMap build(){
        return conceptMap;
    }

}
