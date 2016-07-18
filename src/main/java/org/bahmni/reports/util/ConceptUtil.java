package org.bahmni.reports.util;

import org.apache.log4j.Logger;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.WebClientsException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConceptUtil {

    private static final Logger logger = Logger.getLogger(ConceptUtil.class);

    public static ConceptDataTypes getConceptDataType(String concept, HttpClient httpClient, String openmrsRootUrl) throws
            ConceptDataTypeException {
        try {
            String url = getEncodedUrl(openmrsRootUrl + "/concept/", concept);
            String response = httpClient.get(new URI(url));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode nameNode = rootNode.path("datatype");
            String type = nameNode.path("display").getTextValue();
            try {
                return ConceptDataTypes.valueOf(type);
            } catch (IllegalArgumentException e) {
                return ConceptDataTypes.Others;
            }
        } catch(WebClientsException e){
            logger.error(e);
            throw new ConceptDataTypeException("Concept not found:"+concept);
        } catch (Exception e) {
            logger.error(e);
            throw new ConceptDataTypeException(e.getMessage());
        }
    }


    public static ConceptDataTypes getConceptDataType(List<String> concepts, HttpClient httpClient, String openmrsRootUrl) throws
            ConceptDataTypeException {
        Set<ConceptDataTypes> dataTypes = new HashSet<>();
        for (String concept : concepts) {
            dataTypes.add(getConceptDataType(concept, httpClient, openmrsRootUrl));
        }
        if (dataTypes.size() != 1) {
            return ConceptDataTypes.Others;
        }
        Iterator<ConceptDataTypes> iterator = dataTypes.iterator();
        return iterator.next();
    }

    private static String getEncodedUrl(String baseUri, String concept) {
        try {
            return new URI(null, baseUri + concept, null).toASCIIString();
        } catch (URISyntaxException e) {
            logger.error(e);
            return baseUri + concept;
        }
    }

}
