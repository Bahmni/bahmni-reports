package org.bahmni.reports.extension.icd10.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bahmni.reports.extension.icd10.Icd10Service;
import org.bahmni.reports.extension.icd10.bean.ICDRule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class Icd10ServiceImpl implements Icd10Service {
    public static void main(String[] args) {
        String snomedCode = "16705321000119109";
        Integer offset = 0;
        Integer limit = 10;
        Boolean termActive = true;
        Icd10ServiceImpl service = new Icd10ServiceImpl();
        service.getMapRules(snomedCode, offset, limit, termActive);
    }

    @Override
    public List<ICDRule> getMapRules(String snomedCode, Integer offset, Integer limit, Boolean termActive) {
        String baseUrl = getBaseURL(offset, limit, termActive);
        String eclUrl = getEclUrl(snomedCode);
        String encodedEcl = null;
        StringBuilder sb = new StringBuilder(baseUrl);
        String urls = null;

        try {
            encodedEcl = encode(eclUrl);
            sb.append("&ecl=").append(encodedEcl);
            urls = sb.toString();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        String response = getIcdResponse(urls);
        ObjectMapper mapper = new ObjectMapper();
        List<ICDRule> rules = new ArrayList<>();

        List<ICDRule> sortedRules = null;
        try {
            JsonNode jsonNode = mapper.readValue(response, JsonNode.class);
            JsonNode itemsArr = jsonNode.get("items");

            if (itemsArr.isArray()) {
                for (JsonNode item : itemsArr) {
                    ICDRule rule = mapper.readValue(mapper.writeValueAsString(item), ICDRule.class);
                    rules.add(rule);
                }
            }

            Comparator<ICDRule> customComparator = (rule1, rule2) -> {
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
            };

            sortedRules = rules.stream().sorted(customComparator).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sortedRules;
    }

    private String getBaseURL(Integer offset, Integer limit, Boolean termActive) {
        String baseUrl = "https://browser.ihtsdotools.org/snowstorm/snomed-ct/MAIN/SNOMEDCT-ES/2022-10-31/concepts";
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("?offset=").append(offset)
                .append("&limit=").append(limit)
                .append("&termActive=").append(termActive);
        return sb.toString();
    }

    private String getEclUrl(String referencedComponentId) {
        return "^[*] 447562003 |ICD-10 complex map reference set| {{ M referencedComponentId = \"" + referencedComponentId + "\" }}";
    }

    public String getIcdResponse(String url) {
        String result = null;
        HttpGet request = new HttpGet(url);

        // add request headers
        request.addHeader("content-type", "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {

                    try {
                        result = EntityUtils.toString(entity);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(result);
                }
            }

        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

}
