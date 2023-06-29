package org.bahmni.reports.icd10;

import org.bahmni.reports.icd10.bean.ICDMap;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;

public class ICD10Decorator implements ResultSetDecorator {
    public Collection<ICDMap> enrich(ResultSet resultSet) throws Exception {
        return withIcd10Code(resultSet);
    }
    public Collection<ICDMap> withIcd10Code(ResultSet resultSet) throws Exception {
        Collection<ICDMap> collection = new ArrayList<ICDMap>();
        if (resultSet != null) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                ICDMap map = new ICDMap();
                for (int i = 0; i < columnCount; i++) {
                    int colNextIndex = i + 1;
                    if ("gender".equals(resultSet.getMetaData().getColumnLabel(colNextIndex).toLowerCase())) {
                        map.setGender(resultSet.getString(colNextIndex));
                    } else if ("date of birth".equals(resultSet.getMetaData().getColumnLabel(colNextIndex).toLowerCase())) {
                        String dob = resultSet.getString(colNextIndex);
                        int age = Period.between(LocalDate.parse(dob), LocalDate.now()).getYears();
                        map.setAge(age);
                    } else if ("terminology code".equals(resultSet.getMetaData().getColumnLabel(colNextIndex).toLowerCase())) {
                        map.setSnomedCode(resultSet.getString(colNextIndex));
                    }
                    if (map.hasAllInputs()) {
                        String icd10Code = new ICD10Evaluator().getICDCodes(map.getSnomedCode(), map.getAge(), map.getGender());
                        map.setIcdCodes(icd10Code);
                        collection.add(map);
                        //resultSet.updateString("ICD10 Code",icd10Code);
                        //resultSet.updateRow();
                        break;
                    }
                }
            }
        }
        return collection;
    }

}
