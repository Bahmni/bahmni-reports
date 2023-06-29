package org.bahmni.reports.icd10;

import org.bahmni.reports.icd10.bean.ICDMap;

import java.sql.ResultSet;
import java.util.Collection;

public interface ResultSetDecorator {
    Collection<ICDMap> enrich(ResultSet resultSet) throws Exception;
}
