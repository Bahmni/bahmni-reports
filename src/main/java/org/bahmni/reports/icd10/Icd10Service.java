package org.bahmni.reports.icd10;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("icd10Service")
public interface Icd10Service {
    String searchMapRules(String snomedCode, Integer offset, Integer limit, Boolean termActive);
}
