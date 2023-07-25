package org.bahmni.reports.extensions.icd10;


import org.bahmni.reports.extensions.icd10.Impl.Icd10LookupServiceImpl;
import org.bahmni.reports.extensions.icd10.Impl.Icd10LookupServiceImplTest;
import org.bahmni.reports.extensions.icd10.bean.IcdResponse;
import org.bahmni.reports.extensions.icd10.bean.IcdRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class Icd10EvaluatorTest {
    @InjectMocks
    Icd10Evaluator icd10Evaluator;
    @Mock
    Icd10LookupServiceImpl icd10Service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_ForSingleMapGroup() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_SingleMapGroup.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "M");
        assertNotNull(codes);
        assertEquals("J45.9", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_PerMapGroupForMultipleMapGroups() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_MultipleMapGroups.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "M");
        assertNotNull(codes);
        assertEquals("J45.9,N45.9", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_PerMapGroupForMultipleMapGroups_BasedOnAgeGreaterThanPredicate() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Age.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 90, "M");
        assertNotNull(codes);
        assertEquals("M83.19", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_PerMapGroupForMultipleMapGroups_BasedOnAgeLesserThanPredicate() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Age.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 10, "M");
        assertNotNull(codes);
        assertEquals("E55.0", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_PerMapGroupForMultipleMapGroups_BasedOnAgeRangePredicate() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Age.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 14, "M");
        assertNotNull(codes);
        assertEquals("E55.0,M87.19", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_ForSingleMapGroup_BasedOnMaleGenderPredicate() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Gender.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "M");
        assertNotNull(codes);
        assertEquals("N46", codes);
    }

    @Test
    public void shouldSelectFirstMatchingICDCodeInMapPriorityOrder_ForSingleMapGroup_BasedOnFemaleGenderPredicate() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Gender.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "F");
        assertNotNull(codes);
        assertEquals("N97.9", codes);
    }

    @Test
    public void shouldSelectFallbackICDCodeInMapPriorityOrder_ForSingleMapGroup() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_Gender.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "OTHER");
        assertNotNull(codes);
        assertEquals("", codes);
    }


    @Test
    public void shouldReturnEmptyICDCode_PerMapGroupForMultipleMapGroups_WhenMatchingMapTargetIsEmpty() {
        List<IcdRule> mockSortedRules = getMockMapRules("terminologyServices/icdRules_WithEmptyMapTargets.json");
        when(icd10Service.getRules(any())).thenReturn(mockSortedRules);
        String codes = icd10Evaluator.getMatchingIcdCodes("dummycode", 34, "OTHER");
        assertNotNull(codes);
        assertEquals("", codes);
    }


    List<IcdRule> getMockMapRules(String filePath) {
        IcdResponse icdResponse = Icd10LookupServiceImplTest.getMockIcdResponse(filePath);
        return icdResponse.getItems().stream().sorted(Icd10LookupServiceImpl.customComparator).collect(Collectors.toList());
    }

}