package org.bahmni.reports.icd10;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

public interface ResultSetWrapper {
    Collection<Map<String, ?>> enrich(Collection<Map<String, String>> collection) throws Exception;
}
