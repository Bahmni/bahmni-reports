package org.bahmni.reports.icd10;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.icd10.bean.ICDRule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ICD10Evaluator {
    static List<ICDRule> rules = new ArrayList<>();
    public static void main(String[] args) {
        try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("icd-response2.json")){
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readValue(in, JsonNode.class);
            JsonNode itemsArr = jsonNode.get("items");
            if (itemsArr.isArray()) {
                for (JsonNode item : itemsArr) {
                    ICDRule rule = mapper.readValue(mapper.writeValueAsString(item), ICDRule.class);
                    rules.add(rule);
                }
            }

            //List<ICDRule> rules = mapper.readValue(mapper.writeValueAsString(items), new TypeReference<List<ICDRule>>(){});

            //String jsonString = mapper.writeValueAsString(jsonNode);
            //System.out.println(jsonString);
            System.out.println(rules);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
