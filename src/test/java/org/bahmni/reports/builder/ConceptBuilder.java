package org.bahmni.reports.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptDescription;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

public class ConceptBuilder {
    private final org.openmrs.Concept concept;

    public ConceptBuilder() {
        concept = new Concept();
    }

    public Concept build() {
        return concept;
    }

    public ConceptBuilder withName(String conceptName) {
        ConceptName name = new ConceptName(conceptName, Locale.ENGLISH);
        return withName(name);
    }

    public ConceptBuilder withName(ConceptName name) {
        name.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        concept.setPreferredName(name);
        return this;
    }

    public ConceptBuilder withShortName(String name) {
        concept.setShortName(new ConceptName(name, Context.getLocale()));
        return this;
    }

    public ConceptBuilder withDataTypeId(int dataTypeId) {
        ConceptDatatype conceptDatatype = new ConceptDatatype(dataTypeId);
        concept.setDatatype(conceptDatatype);
        return this;
    }

    public ConceptBuilder withClassId(int classId) {
        concept.setConceptClass(new ConceptClass(classId));
        return this;
    }

    public ConceptBuilder withDescription(String description) {
        Collection<ConceptDescription> conceptDescriptions = new HashSet<>();
        ConceptDescription conceptDescription = new ConceptDescription(description, Locale.ENGLISH);
        conceptDescriptions.add(conceptDescription);
        concept.setDescriptions(conceptDescriptions);
        return this;
    }
}