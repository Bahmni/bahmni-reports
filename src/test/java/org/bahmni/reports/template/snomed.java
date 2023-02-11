package org.bahmni.reports.template;

import ca.uhn.fhir.context.FhirContext;
import org.bahmni.reports.report.integrationtests.BaseIntegrationTest;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public class snomed {
    @Test
    public void fetch(){
        System.out.println(getDescendants("195967001"));
    }
    public List<String> getDescendants(String snomedCode){
        String baseUrl = "https://snowstorm.snomed.mybahmni.in/fhir/";
        String valueSetUrl = "http://snomed.info/sct?fhir_vs=ecl/<<";
        String localeLanguage = "en";
        String valueSetUrlTemplate = "ValueSet/$expand?url={0}{1}&displayLanguage={2}";
        try {
            String relativeUrl = MessageFormat.format(valueSetUrlTemplate, encode(valueSetUrl),snomedCode,localeLanguage);
            String url = baseUrl+relativeUrl;
            //String urlNonEncoded = "https://snowstorm.snomed.mybahmni.in/fhir/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=ecl/<195967001&displayLanguage=en";
            ValueSet valueSet = FhirContext.forR4().newRestfulGenericClient(baseUrl).read().resource(ValueSet.class).withUrl(url).execute();
            List<String> codes = valueSet.getExpansion().getContains().stream().map(item -> item.getCode()).collect(Collectors.toList());
            return codes;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }
}
