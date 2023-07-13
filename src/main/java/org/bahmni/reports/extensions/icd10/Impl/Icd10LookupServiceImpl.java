package org.bahmni.reports.extensions.icd10.Impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.extensions.icd10.Icd10LookupService;
import org.bahmni.reports.extensions.icd10.bean.IcdResponse;
import org.bahmni.reports.extensions.icd10.bean.IcdRule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Icd10LookupServiceImpl implements Icd10LookupService {
    public static final Comparator<IcdRule> customComparator = Comparator.comparingInt((IcdRule rule) -> Integer.parseInt(rule.mapGroup)).thenComparingInt(rule -> Integer.parseInt(rule.mapPriority));
    private static final Logger logger = LogManager.getLogger(Icd10LookupServiceImpl.class);
    private static final String ICD_PROPERTIES_FILENAME = "icd-service-config.properties";
    private static final Properties icd10Properties = loadIcdProperties();
    private RestTemplate restTemplate = new RestTemplate();

    static Properties loadIcdProperties() {
        try (InputStream in = Icd10LookupServiceImpl.class.getClassLoader().getResourceAsStream(ICD_PROPERTIES_FILENAME)) {
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException exception) {
            logger.error("Could not load ICD service properties from: " + ICD_PROPERTIES_FILENAME, exception);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public List<IcdRule> getRules(String snomedCode) {
        try {
            IcdResponse icdResponse;
            List<IcdRule> icdRules = new ArrayList<>();
            int offset = 0, limit = 100;
            do {
                URI encodedURI = getEndPoint(snomedCode, offset, limit);
                icdResponse = getResponse(encodedURI);
                icdRules.addAll(icdResponse.getItems());
                offset += limit;
            } while (offset < icdResponse.getTotal());
            return icdRules.stream().sorted(customComparator).collect(Collectors.toList());
        } catch (Exception exception) {
            logger.error(String.format("Error caused during ICD lookup rest call: %s", exception.getMessage()));
            return new ArrayList<>();
        }

    }

    private String getEclUrl(String referencedComponentId) {
        return String.format(icd10Properties.getProperty("icd.eclUrl"), referencedComponentId);
    }

    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

    private IcdResponse getResponse(URI encodedURI) {
        ResponseEntity<IcdResponse> responseEntity = restTemplate.exchange(encodedURI, HttpMethod.GET, new org.springframework.http.HttpEntity<>(null, getHeaders()), IcdResponse.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        return new IcdResponse();
    }

    private URI getEndPoint(String snomedCode, Integer offset, Integer limit) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(icd10Properties.getProperty("icd.baseUrl"))
                    .queryParam("offset", offset)
                    .queryParam("termActive", true)
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
