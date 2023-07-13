package org.bahmni.reports.extensions.icd10;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.extensions.icd10.Impl.Icd10LookupServiceImpl;
import org.bahmni.reports.extensions.icd10.bean.ICDResponse;
import org.bahmni.reports.extensions.icd10.bean.ICDRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class Icd10EvaluatorTest {
    @InjectMocks
    Icd10Evaluator icd10Evaluator;
    @Mock
    Icd10LookupServiceImpl icd10Service;
    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_ForSingleMapGroup() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_SingleMapGroup.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("J45.9", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_PerMapGroupForMultipleMapGroups() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_MultipleMapGroups.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("J45.9,N45.9", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_PerMapGroupForMultipleMapGroups_BasedOnAgeGreaterThanPredicate() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Age.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 90, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("M83.19", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_PerMapGroupForMultipleMapGroups_BasedOnAgeLesserThanPredicate() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Age.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 10, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("E55.0", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_PerMapGroupForMultipleMapGroups_BasedOnAgeRangePredicate() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Age.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 14, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("E55.0,M87.19", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_ForSingleMapGroup_BasedOnMaleGenderPredicate() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Gender.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "M");
        Assert.assertNotNull(codes);
        Assert.assertEquals("N46", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_ForSingleMapGroup_BasedOnFemaleGenderPredicate() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Gender.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "F");
        Assert.assertNotNull(codes);
        Assert.assertEquals("N97.9", codes);
    }

    @Test
    public void shouldSelectFallbackICDCodeInMapPriorityOrder_ForSingleMapGroup() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Gender.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "OTHER");
        Assert.assertNotNull(codes);
        Assert.assertEquals("", codes);
    }

    @Test
    public void shouldReturnEmptyICDCodeWhenMultipleMapGroupRulesHaveEmptyMapTarget() {
        List<ICDRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_WithEmptyMapTargets.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "OTHER");
        Assert.assertNotNull(codes);
        Assert.assertEquals("", codes);
    }

    private List<ICDRule> getMockMapRules(String filePath) {
        try {
            ICDResponse icdResponse = objectMapper.readValue(readFileAsStr(filePath), ICDResponse.class);
            return icdResponse.getItems().stream().sorted(Icd10LookupServiceImpl.customComparator).collect(Collectors.toList());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readFileAsStr(String relativePath) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(relativePath).toURI());
        return Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
    }
}