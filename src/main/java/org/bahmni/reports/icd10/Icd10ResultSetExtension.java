package org.bahmni.reports.icd10;

import org.bahmni.reports.template.TSIntegrationDiagnosisLineReportTemplate;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Icd10ResultSetExtension implements ResultSetExtension {
    public static final String ICD_10_COLUMN_NAME = "ICD10 Code(s)";
    ICD10Evaluator icd10Evaluator = new ICD10Evaluator();

    public String getColumnName() {
        return ICD_10_COLUMN_NAME;
    }

    public Collection<Map<String, ?>> enrich(Collection<Map<String, String>> rawCollection) throws SQLException {
        Collection<Map<String, ?>> enrichCollection = new ArrayList<>();
        for (Map<String, String> rowMap : rawCollection) {
            String terminologyCode = (String) rowMap.get(TSIntegrationDiagnosisLineReportTemplate.TERMINOLOGY_COLUMN_NAME);
            int age = getAgeFromDob((String) rowMap.get(TSIntegrationDiagnosisLineReportTemplate.PATIENT_DATE_OF_BIRTH_COLUMN_NAME));
            String gender = (String) rowMap.get(TSIntegrationDiagnosisLineReportTemplate.GENDER_COLUMN_NAME);
            String icd10Code = icd10Evaluator.getICDCodes(terminologyCode, age, gender);
            rowMap.put(ICD_10_COLUMN_NAME, icd10Code);
            enrichCollection.add(rowMap);
        }
        return enrichCollection;
    }

    private int getAgeFromDob(String dateOfBirth) {
        return Period.between(LocalDate.parse(dateOfBirth), LocalDate.now()).getYears();
    }
}
