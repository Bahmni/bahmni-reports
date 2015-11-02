package org.bahmni.reports.builder;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;

public class VisitBuilder {

    private final Visit visit = new Visit();

    public Visit build() {
        return visit;
    }

    public VisitBuilder withVisitType(Integer visitTypeId) {
        visit.setVisitType(new VisitType(visitTypeId));
        return this;
    }

    public VisitBuilder withPatient(Patient patient) {
        visit.setPatient(patient);
        return this;
    }

    public VisitBuilder withStartDate(String date) {
        visit.setStartDatetime(DateUtil.parseDate(date));
        return this;
    }
}
