package org.bahmni.reports.icd10;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.icd10.Impl.Icd10ServiceImpl;
import org.bahmni.reports.icd10.bean.ICDRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class ICD10EvaluatorTest {

    @InjectMocks
    ICD10Evaluator icd10Evaluator;
    @Mock
    Icd10ServiceImpl icd10Service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSelectICDCodeWithHighMapPriority() {
        List<ICDRule> mockSortedRules = getMockMapRules("ts/icd-response1.json");
        when(icd10Service.getMapRules(any(), any(), any(), any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getICDCodes("dummycode", 34, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("J45.9", codes);
    }

    @Test
    public void shouldSelectMultipleICDCodeWithHighMapPriorityWhenMultipleMapGroupsPassed() {
        List<ICDRule> mockSortedRules = getMockMapRules("ts/icd-response2.json");
        when(icd10Service.getMapRules(any(), any(), any(), any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getICDCodes("dummycode", 34, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("J45.9,N45.9", codes);
    }

    @Test
    public void shouldSelectICDCodeWithHighMapPriorityWhenAgeIsGreaterThan65() {
        List<ICDRule> mockSortedRules = getMockMapRules("ts/icd-response3.json");
        when(icd10Service.getMapRules(any(), any(), any(), any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getICDCodes("dummycode", 90, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("M83.19", codes);
    }

    @Test
    public void shouldSelectICDCodeWithHighMapPriorityWhenAgeIsLessThan18() {
        List<ICDRule> mockSortedRules = getMockMapRules("ts/icd-response3.json");
        when(icd10Service.getMapRules(any(), any(), any(), any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getICDCodes("dummycode", 10, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("E55.0", codes);
    }

    @Test
    public void shouldSelectICDCodeWithHighMapPriorityWhenAgeIsGreaterThan18AndGreaterThan12() {
        List<ICDRule> mockSortedRules = getMockMapRules("ts/icd-response3.json");
        when(icd10Service.getMapRules(any(), any(), any(), any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getICDCodes("dummycode", 14, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("E55.0,M87.19", codes);
    }

    @Test
    public void shouldSelectICDCodeForMaleWhenMaleGenderPassed() {
        List<ICDRule> mockSortedRules = getMockMapRules("ts/icd-response4.json");
        when(icd10Service.getMapRules(any(), any(), any(), any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getICDCodes("dummycode", 34, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("N46", codes);
    }

    @Test
    public void shouldSelectICDCodeForFemaleWhenFemaleGenderPassed() {
        List<ICDRule> mockSortedRules = getMockMapRules("ts/icd-response4.json");
        when(icd10Service.getMapRules(any(), any(), any(), any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getICDCodes("dummycode", 34, "F");
        Assert.assertNotNull(codes);
        Assert.assertEquals("N97.9", codes);
    }

    @Test
    public void shouldReturnEmptyWhenNoInformationPassed() {
        List<ICDRule> mockSortedRules = getMockMapRules("ts/icd-response4.json");
        when(icd10Service.getMapRules(any(), any(), any(), any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getICDCodes("dummycode", 34, "OTHER");
        Assert.assertNotNull(codes);
        Assert.assertEquals("", codes);
    }

    private List<ICDRule> getMockMapRules(String relativePath) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath)) {
            List<ICDRule> rules = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode jsonNode = mapper.readValue(in, JsonNode.class);
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
            return rules.stream().sorted(customComparator).collect(Collectors.toList());
        } catch (Exception ignored) {

        }
        return new ArrayList<>();
    }


}