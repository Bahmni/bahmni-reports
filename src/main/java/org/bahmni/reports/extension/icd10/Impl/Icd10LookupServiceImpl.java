package org.bahmni.reports.extension.icd10.Impl;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.extension.icd10.Icd10LookupService;
import org.bahmni.reports.extension.icd10.bean.ICDResponse;
import org.bahmni.reports.extension.icd10.bean.ICDRule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class Icd10LookupServiceImpl implements Icd10LookupService {
    private static final Logger logger = LogManager.getLogger(Icd10LookupServiceImpl.class);


    public static void main(String[] args) {
        String snomedCode = "421671002 ";
        Integer offset = 0;
        Integer limit = 10;
        Boolean termActive = true;
        Icd10LookupServiceImpl service = new Icd10LookupServiceImpl();
        service.getRules(snomedCode, offset, limit, termActive);
    }

    @Override
    public List<ICDRule> getRules(String snomedCode, Integer offset, Integer limit, Boolean termActive) {
        String baseUrl = getBaseURL(offset, limit, termActive);
        String eclUrl = getEclUrl(snomedCode);
        String encodedEcl = null;
        StringBuilder sb = new StringBuilder(baseUrl);
        String url = null;

        try {
            encodedEcl = encode(eclUrl);
            sb.append("&ecl=").append(encodedEcl);
            url = sb.toString();
        } catch (Exception exception) {
            // todo
            logger.error(String.format("Error caused during reflection in enrichUsingReflection method: %s", exception.getMessage()));
            throw new RuntimeException();
        }
        MultiValueMap<String, String> map = getMapValue(offset, limit, termActive, snomedCode);

//        String response = getIcdResponse(url);
//        ObjectMapper mapper = new ObjectMapper();
        List<ICDRule> rules = new ArrayList<>();

        List<ICDRule> sortedRules = null;
        try {
            rules = getResponse(baseUrl, map, offset, limit, termActive, snomedCode);


            Comparator<ICDRule> customComparator = (rule1, rule2) -> {
                if (Integer.parseInt(rule1.mapGroup) < Integer.parseInt(rule2.mapGroup)) {
                    return -1;
                }
                if (Integer.parseInt(rule1.mapGroup) > Integer.parseInt(rule2.mapGroup)) {
                    return 1;
                }
                return Integer.compare(Integer.parseInt(rule1.mapPriority), Integer.parseInt(rule2.mapPriority));
            };

            sortedRules = rules.stream().sorted(customComparator).collect(Collectors.toList());

        } catch (Exception exception) {
            // todo
            logger.error(String.format("Error caused during reflection in enrichUsingReflection method: %s", exception.getMessage()));
            throw new RuntimeException();
        }

        return sortedRules;
    }

    private String getBaseURL(Integer offset, Integer limit, Boolean termActive) {
        String baseUrl = "https://browser.ihtsdotools.org/snowstorm/snomed-ct/MAIN/SNOMEDCT-ES/2022-10-31/concepts";
//        StringBuilder sb = new StringBuilder(baseUrl);
//        sb.append("?offset=").append(offset)
//                .append("&limit=").append(limit)
//                .append("&termActive=").append(termActive);
        return baseUrl;
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

        } catch (ClientProtocolException exception) {
            // todo
            logger.error(String.format("Error caused during reflection in enrichUsingReflection method: %s", exception.getMessage()));
            throw new RuntimeException(exception);
        } catch (IOException exception2) {
            // todo
            logger.error(String.format("Error caused during reflection in enrichUsingReflection method: %s", exception2.getMessage()));
            throw new RuntimeException(exception2);
        }
        return result;
    }

    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

    private List<ICDRule> getResponse(String endpoint, MultiValueMap<String, String> map, Integer offset, Integer limit, Boolean termActive, String snomedCode) {
        try {
//            RestTemplate restTemplate = new RestTemplate();
//             org.springframework.http.HttpEntity entity =  new org.springframework.http.HttpEntity<MultiValueMap<String, String>>(map ,
//                    getHeaders());
//            ResponseEntity<ICDResponse> icdResponseResponseEntity = restTemplate.exchange(endpoint, HttpMethod.GET, entity, ICDResponse.class);
//            System.out.println(icdResponseResponseEntity);
//            ResponseEntity<String> stringResponse = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
//            System.out.println(stringResponse);
            RestTemplate restTemplate = new RestTemplate();
//             Define the URL and query parameters
            String url = "https://browser.ihtsdotools.org/snowstorm/snomed-ct/MAIN/SNOMEDCT-ES/2022-10-31/concepts";
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url)
                    .queryParam("offset", String.valueOf(offset))
                    .queryParam("termActive", String.valueOf(termActive))
                    .queryParam("ecl", getEclUrl(snomedCode))
                    .queryParam("limit", String.valueOf(limit));
            URI uri = uriBuilder.build().toUri();
            // Send the GET request and retrieve the response
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET,  new org.springframework.http.HttpEntity<>(null, getHeaders()), String.class);
            restTemplate.getForObject(uriBuilder.toUriString(), String.class);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MultiValueMap<String, String> getMapValue(Integer offset, Integer limit, Boolean termActive, String snomedCode) {
        try {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("offset", String.valueOf(offset));
            map.add("limit", String.valueOf(limit));
            map.add("termActive", String.valueOf(termActive));
            map.add("ecl", encode(getEclUrl(snomedCode)));
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

}
