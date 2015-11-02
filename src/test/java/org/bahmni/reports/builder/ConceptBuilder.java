package org.bahmni.reports.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;

public class ConceptBuilder {
    private final org.openmrs.Concept concept;

    public ConceptBuilder() {
        concept = new Concept();
    }

    public Concept build() {
        return concept;
    }

    public ConceptBuilder withName(String conceptName) {
        ConceptName name = new ConceptName(conceptName, LocaleUtility.getDefaultLocale());
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
}