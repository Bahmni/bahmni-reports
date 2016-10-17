package org.bahmni.reports.util;

import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;

import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

public class PatientAttributesHelper {

    private static final String PERSON_ATTRIBUTE_COLUMN = "GROUP_CONCAT(DISTINCT (IF(person_attribute_type.name = \"%s\", IFNULL(person_attribute_cn.name, person_attribute.value), NULL))) as \"%s\"";


    private String sql;

    private List<String> attributes;

    public PatientAttributesHelper(List<String> patientAttributes){
        this.attributes = patientAttributes;

        String sql = getFileContent("sql/helper/patientAttributes.sql");
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("patientAttributesFrom",constructPatientAttributeColumns());
        sqlTemplate.add("patientAttributesWhere",getWhereClause());
        this.sql = sqlTemplate.render();
    }

    private String constructPatientAttributeColumns(){
        List<String> personAttributes = new ArrayList<>();
        for(String attribute: attributes){
            personAttributes.add(String.format(PERSON_ATTRIBUTE_COLUMN,attribute,attribute));
        }
        return StringUtils.join(personAttributes,',');
    }

    private String getWhereClause() {
        return "\"" + StringUtils.join(attributes, "\", \"") + "\"";
    }

    public String getFromClause(){
        return StringUtils.join(attributes, ',');
    }

    public String getSql() {
        return sql;
    }
}
