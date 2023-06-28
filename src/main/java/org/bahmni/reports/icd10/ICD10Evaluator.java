package org.bahmni.reports.icd10;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.icd10.bean.ICDRule;

import javax.script.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ICD10Evaluator {
    static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    static ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
    static List<ICDRule> rules = new ArrayList<>();


    public static void main(String[] args) {
        int age = 40;
        String gender = "F";

        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("icd-response2.json")) {
            ObjectMapper mapper = new ObjectMapper();
            //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
                    int rule1Weight = Integer.parseInt(rule1.mapGroup + rule1.mapPriority);
                    int rule2Weight = Integer.parseInt(rule2.mapGroup + rule2.mapPriority);
                    int difference = rule1Weight - rule2Weight;
                    System.out.println(rule1Weight + " " + rule2Weight + " " + difference);
                    return difference;
                }
            };
            List<ICDRule> sortedRules =  rules.stream().sorted(customComparator).collect(Collectors.toList());
            System.out.println("-----------------------------");
            sortedRules.forEach(System.out::println);
            System.out.println("-----------------------------");
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

}