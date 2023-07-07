package org.bahmni.reports.extension.icd10.Impl;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


@Component
public class Icd10LookupServiceImpl implements Icd10LookupService {
    private static final Logger logger = LogManager.getLogger(Icd10LookupServiceImpl.class);
    private static final String ICD_PROPERTIES_FILENAME = "icd-service-config.properties";
    private static final Properties icd10Properties = loadIcdProperties();
    RestTemplate restTemplate = new RestTemplate();

    static Properties loadIcdProperties() {
        try (InputStream in = Icd10LookupServiceImpl.class.getClassLoader().getResourceAsStream(ICD_PROPERTIES_FILENAME)) {
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException exception) {
            logger.error("Could not load icd service properties from: " + ICD_PROPERTIES_FILENAME, exception);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public List<ICDRule> getRules(String snomedCode, Integer offset, Integer limit, Boolean termActive) {
        URI encodedURI = getEndPoint(snomedCode, offset, limit, termActive);
        List<ICDRule> rules;
        try {
            rules = getResponse(encodedURI);
        } catch (Exception exception) {
            logger.error(String.format("Error caused during ICD lookup rest call: %s", exception.getMessage()));
            throw new RuntimeException();
        }
        Comparator<ICDRule> customComparator = Comparator.comparingInt((ICDRule rule) -> Integer.parseInt(rule.mapGroup)).thenComparingInt(rule -> Integer.parseInt(rule.mapPriority));
        return rules.stream().sorted(customComparator).collect(Collectors.toList());
    }

    private String getEclUrl(String referencedComponentId) {
        return String.format(icd10Properties.getProperty("icd.eclUrl"), referencedComponentId);
    }

    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

    private List<ICDRule> getResponse(URI encodedURI) {
        ResponseEntity<ICDResponse> responseEntity = restTemplate.exchange(encodedURI, HttpMethod.GET, new org.springframework.http.HttpEntity<>(null, getHeaders()), ICDResponse.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody().getItems();
        }
        return new ArrayList<>();
    }

    private URI getEndPoint(String snomedCode, Integer offset, Integer limit, Boolean termActive) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(icd10Properties.getProperty("icd.baseUrl"))
                    .queryParam("offset", offset)
                    .queryParam("termActive", termActive)
                    .queryParam("ecl", encode(getEclUrl(snomedCode)))
                    .queryParam("limit", limit);
            return uriBuilder.build(true).toUri();
        } catch (Exception exception) {
            logger.error("Error while encoding ecl url ", exception);
            throw new RuntimeException(exception);
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

}
