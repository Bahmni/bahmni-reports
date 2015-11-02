package org.bahmni.reports.builder;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.User;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PatientBuilder {
    private final Patient patient;

    public PatientBuilder() {
        patient = new Patient();
    }

    public Patient build() {
        return patient;
    }

    public PatientBuilder gender(String gender) {
        this.patient.setGender(gender);
        return this;
    }

    public PatientBuilder birthdate(Date birthdate) {
        this.patient.setBirthdate(birthdate);
        return this;
    }

    public PatientBuilder birthdate(String birthdate) {
        this.patient.setBirthdate(DateUtil.parseDate(birthdate));
        return this;
    }

    public PatientBuilder birthdateEstimated(boolean estimated) {
        this.patient.setBirthdateEstimated(estimated);
        return this;
    }

    public PatientBuilder birthdate(Object birthdate, boolean estimated) {
        if (birthdate instanceof Date)
            this.patient.setBirthdate((Date) birthdate);
        else
            this.patient.setBirthdate(DateUtil.parseDate((String) birthdate));
        this.patient.setBirthdateEstimated(estimated);
        return this;
    }

    public PatientBuilder dead(boolean dead) {
        this.patient.setDead(dead);
        return this;
    }

    public PatientBuilder deathDate(Date deathDate) {
        this.patient.setDeathDate(deathDate);
        return this;
    }

    public PatientBuilder deathDate(String deathDate) {
        this.patient.setDeathDate(DateUtil.parseDate(deathDate));
        return this;
    }

    public PatientBuilder causeOfDeath(Concept causeOfDeath) {
        this.patient.setCauseOfDeath(causeOfDeath);
        return this;
    }

    public PatientBuilder name(PersonName pn) {
        pn.setPreferred(true);
        this.patient.addName(pn);
        return this;
    }

    public PatientBuilder address(PersonAddress pa) {
        this.patient.addAddress(pa);
        return this;
    }

    public PatientBuilder attribute(PersonAttribute pa) {
        this.patient.addAttribute(pa);
        return this;
    }

    public PatientBuilder identifier(PatientIdentifier identifier) {
        this.patient.addIdentifier(identifier);
        if (this.patient.getIdentifiers().size() == 1) {
            this.patient.getIdentifiers().iterator().next().setPreferred(true);
        }
        return this;
    }

    public PatientBuilder identifier(PatientIdentifierType identifierType, String identifier, Location location) {
        return this.identifier(new PatientIdentifier(identifier, identifierType, location));
    }

    public PatientBuilder identifier(PatientIdentifierType identifierType, String identifier) {
        return this.identifier(new PatientIdentifier(identifier, identifierType, (Location) null));
    }

    public PatientBuilder uuid(String uuid) {
        this.patient.setUuid(uuid);
        return this;
    }

    public PatientBuilder dateCreated(Date dateCreated) {
        this.patient.setDateCreated(dateCreated);
        return this;
    }

    public PatientBuilder dateCreated(String dateCreated) {
        this.patient.setDateCreated(DateUtil.parseDate(dateCreated));
        return this;
    }

    public PatientBuilder creator(User creator) {
        this.patient.setCreator(creator);
        return this;
    }

    public PatientBuilder changedBy(User by) {
        this.patient.setChangedBy(by);
        return this;
    }

    public PatientBuilder dateChanged(Date changed) {
        this.patient.setDateChanged(changed);
        return this;
    }

    public PatientBuilder dateChanged(String changed) {
        this.patient.setDateChanged(DateUtil.parseDate(changed));
        return this;
    }

    public PatientBuilder voided(boolean voided) {
        this.patient.setVoided(Boolean.valueOf(voided));
        return this;
    }

    public PatientBuilder voidedBy(User voidedBy) {
        this.patient.setVoidedBy(voidedBy);
        return this;
    }

    public PatientBuilder dateVoided(Date dateVoided) {
        this.patient.setDateCreated(dateVoided);
        return this;
    }

    public PatientBuilder dateVoided(String dateVoided) {
        this.patient.setDateCreated(DateUtil.parseDate(dateVoided));
        return this;
    }

    public PatientBuilder voidReason(String voidReason) {
        this.patient.setVoidReason(voidReason);
        return this;
    }

    public PatientBuilder name(String given, String family) {
        return this.name(new PersonName(given, (String) null, family));
    }

    public PatientBuilder address(String... addressFields) {
        List fields = Arrays.asList(addressFields);
        Iterator i = fields.iterator();
        PersonAddress address = new PersonAddress();
        if (i.hasNext())
            address.setAddress1((String) i.next());

        if (i.hasNext())
            address.setAddress2((String) i.next());

        if (i.hasNext())
            address.setCityVillage((String) i.next());

        if (i.hasNext())
            address.setStateProvince((String) i.next());

        if (i.hasNext())
            address.setPostalCode((String) i.next());

        if (i.hasNext())
            address.setCountry((String) i.next());

        this.patient.addAddress(address);
        return this;
    }

    public PatientBuilder age(int years) {
        this.patient.setBirthdateFromAge(years, (Date) null);
        return this;
    }

    public PatientBuilder female() {
        this.patient.setGender("F");
        return this;
    }

    public PatientBuilder male() {
        this.patient.setGender("M");
        return this;
    }

    public PatientBuilder clearIdentifiers() {
        if (this.patient.getIdentifiers() != null) {
            this.patient.getIdentifiers().clear();
        }
        return this;
    }

    public PatientBuilder personAttribute(PersonAttributeType attributeType, String value) {
        this.patient.addAttribute(new PersonAttribute(attributeType, value));
        return this;
    }
}
