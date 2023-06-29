package org.bahmni.reports.icd10.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.icd10.bean.ICDRule;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class Icd10ServiceImpl  {

    private String mapRules;

    public String getMapRules() {
        return mapRules;
    }

    public void setMapRules(String mapRules) {
        this.mapRules = mapRules;
    }


    public static void main(String[] args) {

       searchMapRules("8619003",0,100,true);
//        getResponse();
//        searchMapTest();
      //  searchMapTest();

    };

    private static String getResponse(){
        String res = "String to return";
        System.out.println("Test");
        return res;
    }


    private static String searchMapTest(){
        String testString = "Testing -- ";
//        System.out.println("We are here!!!");
        return testString;
    }



    //@Override
    public static String searchMapRules(String snomedCode, Integer offset, Integer limit, Boolean termActive) {
        String baseUrl = getBaseURL(offset,limit,termActive);
        String eclUrl = getEclUrl(snomedCode);
        String encodedEcl = null;
        StringBuilder sb = new StringBuilder(baseUrl);
        String Urls= null;
        try{
            encodedEcl = encode(eclUrl);
            sb.append("&ecl=").append(encodedEcl);
            System.out.println("Relative URl "+ sb.toString());
            Urls = sb.toString();
           // String jsonResponse = sendGETRequest(Urls);
           // System.out.println("This is the Response " + jsonResponse);
        }catch(Exception exception){
              exception.getMessage();
        }

        String Urlss = "https://browser.ihtsdotools.org/snowstorm/snomed-ct/MAIN/SNOMEDCT-ES/2022-10-31/concepts?offset=0&limit=100&termActive=true&ecl=%5E%5B*%5D%20447562003%20%7CICD-10%20complex%20map%20reference%20set%7C%20%7B%7B%20M%20referencedComponentId%20%3D%20%2295208000%22%20%7D%7D";
        String response = sendGETRequest(Urls);
        //TODO convert this to response

        ObjectMapper mapper = new ObjectMapper();
        List<ICDRule> rules = new ArrayList<>();
        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try{
            JsonNode jsonNode = mapper.readValue(response, JsonNode.class);
            JsonNode itemsArr = jsonNode.get("items");
            System.out.println("itemsArr" + itemsArr);
            if (itemsArr.isArray()) {
                for (JsonNode item : itemsArr) {
                    ICDRule rule = mapper.readValue(mapper.writeValueAsString(item), ICDRule.class);
                    System.out.println("These are the rule" + rule);
                    rules.add(rule);
                    System.out.println("This is the rules" + rules);
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
            System.out.println("Sorted list"+ sortedRules );
        }catch (Exception e){
            e.getMessage();
        }
        System.out.println("Thi is the response" + response);
        return sendGETRequest(Urlss);
    }

    private static String getBaseURL(Integer offset, Integer limit, Boolean termActive){
        String baseUrl = "https://browser.ihtsdotools.org/snowstorm/snomed-ct/MAIN/SNOMEDCT-ES/2022-10-31/concepts";
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("?offset=").append(offset)
                .append("&limit=").append(limit)
                .append("&termActive=").append(termActive);
        System.out.println("Url" + sb.toString());
         return sb.toString();
    }

    private static String getEclUrl(String referencedComponentId){
        String eclUrl = "^[*] 447562003 |ICD-10 complex map reference set| {{ M referencedComponentId = \"" + referencedComponentId + "\" }}";
        return eclUrl;
    };

    private static String sendGETRequest(String urlString) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }


    private static String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

}
