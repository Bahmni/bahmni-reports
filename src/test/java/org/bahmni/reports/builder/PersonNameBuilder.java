package org.bahmni.reports.builder;

import org.openmrs.PersonName;

public class PersonNameBuilder {

    private final PersonName personName;

    public PersonNameBuilder() {
        personName = new PersonName();
    }

    public PersonNameBuilder withGivenName(String givenName) {
        personName.setGivenName(givenName);
        return this;
    }

    public PersonNameBuilder withFamilyName(String familyName) {
        personName.setFamilyName(familyName);
        return this;
    }

    public PersonNameBuilder withMiddleName(String middleName) {
        personName.setMiddleName(middleName);
        return this;
    }

    public PersonName build() {
        return personName;
    }

}
