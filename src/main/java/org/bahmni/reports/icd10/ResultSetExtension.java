package org.bahmni.reports.icd10;

import java.util.Collection;
import java.util.Map;

public interface ResultSetExtension {
    Collection<Map<String, ?>> enrich(Collection<Map<String, String>> collection) throws Exception;

    String getColumnName();
}
