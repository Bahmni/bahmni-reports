package org.bahmni.reports.extension.icd10;

import org.bahmni.reports.extension.icd10.bean.ICDRule;

import java.util.List;

public interface Icd10LookupService {
    List<ICDRule> getRules(String snomedCode, Integer offset, Integer limit, Boolean termActive);
}
