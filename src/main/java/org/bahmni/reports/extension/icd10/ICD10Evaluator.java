package org.bahmni.reports.extension.icd10;


import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.extension.icd10.Impl.Icd10LookupServiceImpl;
import org.bahmni.reports.extension.icd10.bean.ICDRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ICD10Evaluator {
    static final String TRUE_KEYWORD = "TRUE";
    static final String FALSE_KEYWORD = "FALSE";
    static final String OTHERWISE_TRUE_KEYWORD = "OTHERWISE TRUE";
    static final String MALE_KEYWORD = "IFA 248153007 | Male (finding) |";
    static final String FEMALE_KEYWORD = "IFA 248152002 | Female (finding) |";
    static final String AGE_KEYWORD = "IFA 445518008 | Age at onset of clinical finding (observable entity) |";
    static final String AND_KEYWORD = "AND";
    static final String AND_KEYWORD_REPLACE = "&&";
    static final String OR_KEYWORD = "OR";
    static final String OR_KEYWORD_REPLACE = "||";
    static final String YEARS_KEYWORD = " years";
    static final String YEARS_KEYWORD_REPLACE = "";
    ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("Nashorn");
    Icd10LookupService icd10LookUpService = new Icd10LookupServiceImpl();


    static String convertRule(String rule, Integer age, String gender) {
        rule = rule.replace(OTHERWISE_TRUE_KEYWORD, TRUE_KEYWORD);
        // todo
        rule = rule.replace(MALE_KEYWORD, gender.equalsIgnoreCase("M") ? TRUE_KEYWORD.toLowerCase() : FALSE_KEYWORD.toLowerCase());
        rule = rule.replace(FEMALE_KEYWORD, gender.equalsIgnoreCase("F") ? TRUE_KEYWORD.toLowerCase() : FALSE_KEYWORD.toLowerCase());
        rule = rule.replace(AGE_KEYWORD, age.toString());
        rule = rule.replace(AND_KEYWORD, AND_KEYWORD_REPLACE);
        rule = rule.replace(OR_KEYWORD, OR_KEYWORD_REPLACE);
        rule = rule.replace(YEARS_KEYWORD, YEARS_KEYWORD_REPLACE);
        return rule;
    }

    public String getICDCodes(String snomedCode, int age, String gender) {
        try {
            List<ICDRule> sortedRules = icd10LookUpService.getRules(snomedCode, 0, 100, true);
            List<String> selectedCodes = new ArrayList<>();
            String mapGroup = "";
            boolean isFound = false;
            for (ICDRule rule : sortedRules) {
                if (!mapGroup.equalsIgnoreCase(rule.getMapGroup())) {
                    mapGroup = rule.getMapGroup();
                    isFound = false;
                }
                if (mapGroup.equalsIgnoreCase(rule.getMapGroup()) && isFound) {
                    continue;
                }
                if (mapGroup.equalsIgnoreCase(rule.getMapGroup()) && !isFound) {
                    if (evaluateRule(convertRule(rule.getMapRule(), age, gender))) {
                        selectedCodes.add(rule.getMapTarget());
                        isFound = true;
                    }
                }
            }
            return selectedCodes.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(","));

        } catch (Exception ignored) {

        }
        return "";
    }

    boolean evaluateRule(String rule) throws ScriptException {
        if (rule.equalsIgnoreCase(TRUE_KEYWORD)) {
            return true;
        }
        if (rule.equalsIgnoreCase(FALSE_KEYWORD)) {
            return false;
        }
        return (boolean) scriptEngine.eval(rule);
    }
}