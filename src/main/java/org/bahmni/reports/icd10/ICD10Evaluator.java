package org.bahmni.reports.icd10;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.icd10.bean.ICDRule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ICD10Evaluator {
    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
    static List<ICDRule> rules = new ArrayList<>();
    int age = 40;
    String gender = "F";

    public static void main(String[] args) {
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
            sortedRules.forEach(System.out::println);
            /*
            String currentMapGroup = "1";
            boolean isMapTargetFound = false;
            List<String> mapTargets = new ArrayList<>();
            for (ICDRule rule : sortedRules) {
                if (!currentMapGroup.equals(rule.getMapGroup())) {
                    currentMapGroup = rule.getMapGroup();
                    isMapTargetFound = false;
                } else if (isMapTargetFound) continue;
                String mapTarget = evaluate(rule.getMapRule());
                if (mapTarget != null) {
                    mapTargets.add(mapTarget);
                    isMapTargetFound = true;
                }
            }
             */



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
    private static String evaluate(String mapRule) {
        String executableMapRule = mapRule.replace("IFA 248153007 | Male (finding) |", "gender=='M'")
                .replace("IFA 248152002 | Female (finding) |", "gender=='F'")
                .replace("IFA 445518008 | Age at onset of clinical finding (observable entity) |", "age")
                .replace("AND", "&&")
                .replace("OR", "||")
                .replace(" years", "");
        System.out.println(executableMapRule);
        return null;
    }
     */
}