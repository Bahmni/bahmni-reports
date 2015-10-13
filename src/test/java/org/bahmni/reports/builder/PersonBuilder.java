package org.bahmni.reports.builder;

import java.util.Set;

import org.openmrs.Person;
import org.openmrs.PersonName;

public class PersonBuilder {

    private final Person person;

    public PersonBuilder() {
        person = new Person();
    }

    public PersonBuilder withUUID(String patientUuid) {
        person.setUuid(patientUuid);
        return this;
    }

    public PersonBuilder withPersonName(Set<PersonName> personNames) {
        person.setNames(personNames);
        return this;
    }

    public Person build() {
        return person;
    }

}
