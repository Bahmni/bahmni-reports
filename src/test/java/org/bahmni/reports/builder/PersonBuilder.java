package org.bahmni.reports.builder;

import org.openmrs.Person;
import org.openmrs.PersonName;

import java.util.Set;

public class PersonBuilder {

    private final Person person;

    public PersonBuilder() {
        person = new Person();
    }

    public Person build() {
        return person;
    }

    public PersonBuilder withPersonName(Set<PersonName> personNames) {
        person.setNames(personNames);
        return this;
    }

    public PersonBuilder withGender(String gender) {
        person.setGender(gender);
        return this;
    }
}
