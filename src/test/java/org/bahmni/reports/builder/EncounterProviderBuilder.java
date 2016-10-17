package org.bahmni.reports.builder;


import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;

public class EncounterProviderBuilder {
    private final EncounterProvider encounterProvider;

    public EncounterProviderBuilder() {
        encounterProvider = new EncounterProvider();
    }

    public EncounterProvider build() {
        return encounterProvider;
    }

    public EncounterProviderBuilder buildWithEncounter(Encounter encounter){
        this.encounterProvider.setEncounter(encounter);
        return this;
    }

    public EncounterProviderBuilder buildWithProvider(Provider provider){
        this.encounterProvider.setProvider(provider);
        return this;
    }

    public EncounterProviderBuilder buildWithEncounterRoleId(EncounterRole encounterRole){
        this.encounterProvider.setEncounterRole(encounterRole);
        return this;
    }

    public EncounterProviderBuilder buildWithVoided(Boolean voided){
        this.encounterProvider.setVoided(voided);
        return this;
    }

}
