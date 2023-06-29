package org.bahmni.reports.icd10;

import org.bahmni.reports.icd10.bean.ICDRule;

import java.util.List;

public interface Icd10Service {
    List<ICDRule> getMapRules(String snomedCode, Integer offset, Integer limit, Boolean termActive);
}
