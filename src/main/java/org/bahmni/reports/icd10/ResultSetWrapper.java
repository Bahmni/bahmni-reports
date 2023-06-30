package org.bahmni.reports.icd10;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

public interface ResultSetWrapper {
    Collection<Map<String, ?>> enrich(ResultSet resultSet) throws Exception;
}
