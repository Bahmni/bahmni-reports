package org.bahmni.reports.icd10;

import org.bahmni.reports.template.TSIntegrationDiagnosisLineReportTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ICD10Wrapper implements ResultSetWrapper {
    private ICD10Evaluator icd10Evaluator;

    private ICD10Wrapper() {
        this.icd10Evaluator = new ICD10Evaluator();
    }

    private static class SingletonHelper {
        private static final ICD10Wrapper INSTANCE = new ICD10Wrapper();
    }

    public static ICD10Wrapper getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public Collection<Map<String, ?>> enrich(ResultSet resultSet) throws SQLException {
        Collection<Map<String, ?>> collection = new ArrayList<Map<String, ?>>();
        if (resultSet != null) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                Map<String, String> resulSetMap = new HashMap<String, String>();
                for (int colNextIndex = 1; colNextIndex <= columnCount; colNextIndex++) {
                    resulSetMap.put(resultSet.getMetaData().getColumnLabel(colNextIndex), resultSet.getString(colNextIndex));
                }
                String terminologyCode = resulSetMap.get(TSIntegrationDiagnosisLineReportTemplate.TERMINOLOGY_COLUMN_NAME);
                int age = getAgeFromDob(resulSetMap.get(TSIntegrationDiagnosisLineReportTemplate.PATIENT_DATE_OF_BIRTH_COLUMN_NAME));
                String gender = resulSetMap.get(TSIntegrationDiagnosisLineReportTemplate.GENDER_COLUMN_NAME);
                String icd10Code = icd10Evaluator.getICDCodes(terminologyCode, age, gender);
                resulSetMap.put("ICD10 Code", icd10Code);
                collection.add(resulSetMap);
            }
        }
        return collection;
    }

    private int getAgeFromDob(String dob) {
        return Period.between(LocalDate.parse(dob), LocalDate.now()).getYears();
    }
}
