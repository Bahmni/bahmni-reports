package org.bahmni.reports.util;

import org.bahmni.webclients.HttpClient;
import org.junit.Test;
import org.mockito.Mock;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConceptUtilTest {


    @Mock
    HttpClient httpClient;

    private static String responseBoolean = "{\"datatype\": {\n" +
            "        \"uuid\": \"8d4a5cca-c2cc-11de-8d13-0010c6dffd0f\",\n" +
            "        \"display\": \"Boolean\",\n" +
            "        \"links\": [\n" +
            "            {\n" +
            "                \"uri\": \"NEED-TO-CONFIGURE/ws/rest/v1/conceptdatatype/8d4a5cca-c2cc-11de-8d13-0010c6dffd0f\",\n" +
            "                \"rel\": \"self\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }}";


    private String openmrsRootUrl = "http://localhost:8080/openmrs/ws/rest/v1";

    @Test
    public void shouldGetConceptDataType() throws ConceptDataTypeException, URISyntaxException {
        initMocks(this);
        when(httpClient.get(new URI("http://localhost:8080/openmrs/ws/rest/v1/concept/concept"))).thenReturn(responseBoolean);
        ConceptDataTypes content = ConceptUtil.getConceptDataType("concept", httpClient, openmrsRootUrl);
        assertEquals(content, ConceptDataTypes.Boolean);
    }

    @Test
    public void shouldRetrunOnlyOneConceptDataType() throws ConceptDataTypeException, URISyntaxException {
        initMocks(this);
        when(httpClient.get(new URI("http://localhost:8080/openmrs/ws/rest/v1/concept/concept1"))).thenReturn(responseBoolean);
        when(httpClient.get(new URI("http://localhost:8080/openmrs/ws/rest/v1/concept/concept2"))).thenReturn(responseBoolean);
        ConceptDataTypes content = ConceptUtil.getConceptDataType(Arrays.asList("concept1", "concept2"), httpClient, openmrsRootUrl);
        assertEquals(content, ConceptDataTypes.Boolean);
    }
}
