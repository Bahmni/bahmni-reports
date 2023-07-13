package org.bahmni.reports.extensions.icd10;

import org.bahmni.reports.extensions.icd10.bean.IcdRule;

import java.util.List;

public interface Icd10LookupService {
    List<IcdRule> getRules(String snomedCode);
}
