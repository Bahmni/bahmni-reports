package org.bahmni.reports.icd10;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.icd10.bean.ICDRule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ICD10Evaluator {
    static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    static ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("Nashorn");
    static List<ICDRule> rules = new ArrayList<>();
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
    static final String YEARS_KEYWORD_REPLACE= "";
    int age = 40;
    String gender = "F";


    public static void main(String[] args) {
        int age = 40;
        String gender = "F";
        ICD10Evaluator icd10Evaluator = new ICD10Evaluator();
        String icdCodes = icd10Evaluator.getICDCodes("", age, gender);


    }

    public String getICDCodes(String snomedCode, int age, String gender){
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("icd-response2.json")) {
            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode jsonNode = mapper.readValue(in, JsonNode.class);
            JsonNode itemsArr = jsonNode.get("items");
            if (itemsArr.isArray()) {
                for (JsonNode item : itemsArr) {
                    ICDRule rule = mapper.readValue(mapper.writeValueAsString(item), ICDRule.class);
                    rules.add(rule);
                }
            }
            Comparator<ICDRule> customComparator = new Comparator<ICDRule>() {
                @Override
                public int compare(ICDRule rule1, ICDRule rule2) {
                    if (Integer.parseInt(rule1.mapGroup) < Integer.parseInt(rule2.mapGroup)) {
                        return -1;
                    }
                    if (Integer.parseInt(rule1.mapGroup) > Integer.parseInt(rule2.mapGroup)) {
                        return 1;
                    }
                    if (Integer.parseInt(rule1.mapPriority) < Integer.parseInt(rule2.mapPriority)) {
                        return -1;
                    }
                    if (Integer.parseInt(rule1.mapPriority) > Integer.parseInt(rule2.mapPriority)) {
                        return 1;
                    }
                    return Integer.compare(Integer.parseInt(rule1.mapTarget), Integer.parseInt(rule2.mapTarget));
                }
            };
            List<ICDRule> sortedRules =  rules.stream().sorted(customComparator).collect(Collectors.toList());
            System.out.println("-----------------------------");
            sortedRules.forEach(System.out::println);
            System.out.println("-----------------------------");
            /*
            String currentMapGroup = "1";
            boolean isMapTargetFound = false;
            List<String> mapTargets = new ArrayList<>();
            for (ICDRule rule : sortedRules) {
                if (!currentMapGroup.equals(rule.getMapGroup())) {
                    currentMapGroup = rule.getMapGroup();
                    isMapTargetFound = false;
                } else if (isMapTargetFound) continue;
                String mapTarget = evaluate(rule,age,gender);
                if (mapTarget != null) {
                    mapTargets.add(mapTarget);
                    isMapTargetFound = true;
                }
            }
            System.out.println("-----------------------------");
            mapTargets.stream().forEach(System.out::println);
            return String.join(",", mapTargets);
        } catch (Exception e) {
            throw new RuntimeException(e);
             */


            //String jsonString = mapper.writeValueAsString(jsonNode);
            //System.out.println(jsonString);
            int patientAge = 34;
            String patientGender = "F";
            List<String> selectedCodes = new ArrayList<>();
            String mapGroup = "";
            boolean isFound = false;
            for(ICDRule rule : sortedRules) {
                if (!mapGroup.equalsIgnoreCase(rule.getMapGroup())) {
                    mapGroup = rule.getMapGroup();
                    isFound = false;
                }
                if(mapGroup.equalsIgnoreCase(rule.getMapGroup()) && isFound) {
                    continue;
                }
                if(mapGroup.equalsIgnoreCase(rule.getMapGroup()) && !isFound) {
                    if(evaluateRule(convertRule(rule.getMapRule(), patientAge, patientGender))) {
                        selectedCodes.add(rule.getMapTarget());
                        isFound =true;
                    }
                }
            }
            System.out.println(selectedCodes);
            return selectedCodes.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(","));

        }
        catch(Exception e){

        }
        return "";
    }


    private static String evaluate(ICDRule rule, int age, String gender) {
        String mapRule = rule.getMapRule();
        String expression = mapRule.replace("IFA 248153007 | Male (finding) |", "gender=='M'")
                .replace("IFA 248152002 | Female (finding) |", "gender=='F'")
                .replace("IFA 445518008 | Age at onset of clinical finding (observable entity) |", "age")
                .replace("OTHERWISE TRUE", "true")
                .replace("TRUE", "true")
                .replace("AND", "&&")
                .replace("OR", "||")
                .replace(" years", "");
        System.out.println(expression);
        try {
            Bindings bindings = new SimpleBindings();
            bindings.put("age", age);
            bindings.put("gender", gender);
            Boolean result = (Boolean) scriptEngine.eval(expression,bindings);
            if(result) return rule.getMapTarget();
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    static String convertRule(String rule, Integer age, String gender) {
        rule = rule.replace(OTHERWISE_TRUE_KEYWORD, TRUE_KEYWORD);
        rule = rule.replace(OTHERWISE_TRUE_KEYWORD, TRUE_KEYWORD);
        rule = rule.replace(MALE_KEYWORD, gender.equalsIgnoreCase("M") ? TRUE_KEYWORD : FALSE_KEYWORD );
        rule = rule.replace(FEMALE_KEYWORD, gender.equalsIgnoreCase("F") ? TRUE_KEYWORD : FALSE_KEYWORD );
        rule = rule.replace(AGE_KEYWORD, age.toString());
        rule = rule.replace(AND_KEYWORD, AND_KEYWORD_REPLACE);
        rule = rule.replace(OR_KEYWORD, OR_KEYWORD_REPLACE);
        rule = rule.replace(YEARS_KEYWORD, YEARS_KEYWORD_REPLACE);
        return rule;
    }
   static boolean evaluateRule(String rule) throws ScriptException {
        if (rule.equalsIgnoreCase(TRUE_KEYWORD)) {
            return true;
        }
       if (rule.equalsIgnoreCase(FALSE_KEYWORD)) {
           return false;
       }
        return (boolean) scriptEngine.eval(rule);
    }
}