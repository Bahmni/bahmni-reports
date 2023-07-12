package org.bahmni.reports.extensions.icd10;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.extensions.icd10.Impl.Icd10LookupServiceImpl;
import org.bahmni.reports.extensions.icd10.bean.ICDRule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Icd10Evaluator {
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
    private static final Logger logger = LogManager.getLogger(Icd10Evaluator.class);
    public Icd10LookupService icd10LookUpService = new Icd10LookupServiceImpl();
    public ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("Nashorn");

    private static int getMapGroup(ICDRule rule) {
        return Integer.parseInt(rule.getMapGroup());
    }

    static String deriveRuleExpression(String rule, Integer age, String gender) {
        rule = rule.replace(OTHERWISE_TRUE_KEYWORD, TRUE_KEYWORD);
        rule = rule.replace(MALE_KEYWORD, gender.equalsIgnoreCase("M") ? TRUE_KEYWORD.toLowerCase() : FALSE_KEYWORD.toLowerCase());
        rule = rule.replace(FEMALE_KEYWORD, gender.equalsIgnoreCase("F") ? TRUE_KEYWORD.toLowerCase() : FALSE_KEYWORD.toLowerCase());
        rule = rule.replace(AGE_KEYWORD, age.toString());
        rule = rule.replace(AND_KEYWORD, AND_KEYWORD_REPLACE);
        rule = rule.replace(OR_KEYWORD, OR_KEYWORD_REPLACE);
        rule = rule.replace(YEARS_KEYWORD, YEARS_KEYWORD_REPLACE);
        return rule;
    }

    private static boolean isRuleWithNextMapGroup(int currentMapGroup, ICDRule rule) {
        int ruleMapGroup = getMapGroup(rule);
        return ruleMapGroup > currentMapGroup;
    }

    public String getMatchingIcdCodes(String snomedCode, int age, String gender) {
        List<String> matchingICDCodes = new ArrayList<>();
        try {
            List<ICDRule> orderedRules = icd10LookUpService.getRules(snomedCode);
            int currentMapGroup = 1;
            boolean isMapTargetFoundForCurrentMapGroup = false;
            for (ICDRule rule : orderedRules) {
                if (isRuleWithNextMapGroup(currentMapGroup, rule)) {
                    currentMapGroup = getMapGroup(rule);
                    isMapTargetFoundForCurrentMapGroup = false;
                }
                if (isMapTargetFoundForCurrentMapGroup) {
                    continue;
                }
                if (evaluateRule(deriveRuleExpression(rule.getMapRule(), age, gender))) {
                    matchingICDCodes.add(rule.getMapTarget());
                    isMapTargetFoundForCurrentMapGroup = true;
                }
            }
        } catch (Exception exception) {
            logger.error("Error occurred when extracting Icd10 codes for snomed code: " + snomedCode, exception);
        }
        return matchingICDCodes.stream().collect(Collectors.joining(","));
    }

    boolean evaluateRule(String rule) {
        try {
            if (rule.equalsIgnoreCase(TRUE_KEYWORD)) {
                return true;
            }
            if (rule.equalsIgnoreCase(FALSE_KEYWORD)) {
                return false;
            }
            return (boolean) scriptEngine.eval(rule);
        } catch (Exception exception) {
            logger.error("Error occurred during evaluating rules", exception);
            throw new RuntimeException(exception);
        }
    }
}