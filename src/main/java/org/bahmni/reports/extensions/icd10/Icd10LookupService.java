package org.bahmni.reports.extensions.icd10;

import org.bahmni.reports.extensions.icd10.bean.ICDRule;

import java.util.List;

public interface Icd10LookupService {
    List<ICDRule> getRules(String snomedCode);
}