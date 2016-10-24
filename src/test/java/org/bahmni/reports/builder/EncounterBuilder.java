package org.bahmni.reports.builder;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Visit;

public class EncounterBuilder {
    private final Encounter encounter;

    public EncounterBuilder() {
        encounter = new Encounter();
    }

    public Encounter build() {
        return encounter;
    }

    public EncounterBuilder withVisit(Visit visit) {
        encounter.setVisit(visit);
        return this;
    }

    public EncounterBuilder withPatient(Patient patient) {
        encounter.setPatient(patient);
        return this;
    }

    public EncounterBuilder withDatetime(String dateTime) {
        encounter.setEncounterDatetime(DateUtil.parseDate(dateTime));
        return this;
    }

    public EncounterBuilder withDateCreated(String dateTime) {
        encounter.setDateCreated(DateUtil.parseDate(dateTime));
        return this;
    }

    public EncounterBuilder withEncounterType(EncounterType encounterType) {
        encounter.setEncounterType(encounterType);
        return this;
    }
}
