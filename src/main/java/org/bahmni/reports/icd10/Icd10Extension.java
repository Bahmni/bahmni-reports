package org.bahmni.reports.icd10;

import org.bahmni.reports.template.TSIntegrationDiagnosisLineReportTemplate;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Icd10Extension implements ResultSetWrapper {
    ICD10Evaluator icd10Evaluator;

    public Icd10Extension() {
        this.icd10Evaluator = new ICD10Evaluator();
    }

    public Collection<Map<String, ?>> enrich(Collection<Map<String, String>> rawCollection) throws SQLException {
        Collection<Map<String, ?>> enrichedCollection = new ArrayList<Map<String, ?>>();
        for(Map<String, String> map:rawCollection){
            String terminologyCode = (String)map.get(TSIntegrationDiagnosisLineReportTemplate.TERMINOLOGY_COLUMN_NAME);
            int age = getAgeFromDob((String)map.get(TSIntegrationDiagnosisLineReportTemplate.PATIENT_DATE_OF_BIRTH_COLUMN_NAME));
            String gender = (String)map.get(TSIntegrationDiagnosisLineReportTemplate.GENDER_COLUMN_NAME);
            String icd10Code = icd10Evaluator.getICDCodes(terminologyCode, age, gender);
            map.put("ICD10 Code", icd10Code);
            enrichedCollection.add(map);
        }
        return enrichedCollection;
    }

    /*
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
     */

    private int getAgeFromDob(String dob) {
        return Period.between(LocalDate.parse(dob), LocalDate.now()).getYears();
    }
}
