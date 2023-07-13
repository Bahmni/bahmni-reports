package org.bahmni.reports.extensions.icd10.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.extensions.icd10.bean.IcdResponse;
import org.bahmni.reports.extensions.icd10.bean.IcdRule;
import org.bahmni.reports.util.FileReaderUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
public class Icd10LookupServiceImplTest {
    private static final String SNOMED_CODE = "dummy";
    private static ObjectMapper mapper = new ObjectMapper();
    @InjectMocks
    Icd10LookupServiceImpl icd10LookupService;
    @Mock
    ResponseEntity<IcdResponse> mockIcdResponse;
    @Mock
    RestTemplate restTemplate;

    public static IcdResponse getMockIcdResponse(String relativePath) {
        try {
            return mapper.readValue(FileReaderUtil.getFileContent(relativePath), IcdResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnIcdRulesOrderedByMapGroupAndThenMapPriority_WhenValidSnomedCodeIsPassed() {
        IcdResponse mockResponse = getMockIcdResponse("terminologyServices/icdRules_MultipleMapGroups.json");
        when(restTemplate.exchange(any(), any(), any(), eq(IcdResponse.class))).thenReturn(mockIcdResponse);
        when(mockIcdResponse.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockIcdResponse.getBody()).thenReturn(mockResponse);
        List<IcdRule> sortedRules = icd10LookupService.getRules(SNOMED_CODE);
        assertNotNull(sortedRules);
        assertEquals(4, sortedRules.size());
        assertEquals("1", sortedRules.get(0).getMapGroup());
        assertEquals("1", sortedRules.get(0).getMapPriority());
        assertEquals("1", sortedRules.get(1).getMapGroup());
        assertEquals("2", sortedRules.get(1).getMapPriority());
        assertEquals("2", sortedRules.get(2).getMapGroup());
        assertEquals("1", sortedRules.get(2).getMapPriority());
        assertEquals("2", sortedRules.get(3).getMapGroup());
        assertEquals("2", sortedRules.get(3).getMapPriority());
    }

    @Test
    public void shouldReturnEmptyList_WhenInvalidSnomedCodeIsPassed() {
        when(restTemplate.exchange(any(), any(), any(), eq(IcdResponse.class))).thenReturn(mockIcdResponse);
        when(mockIcdResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        List<IcdRule> sortedRules = icd10LookupService.getRules(SNOMED_CODE);
        assertNotNull(sortedRules);
        assertEquals(0, sortedRules.size());
    }

    @Test
    public void shouldInvokePaginatedCalls_WhenIcdRulesHasLargeResultSet() {
        IcdResponse mockResponse = getMockIcdResponse("terminologyServices/icdRules_WithLargeResultSet.json");
        when(restTemplate.exchange(any(), any(), any(), eq(IcdResponse.class))).thenReturn(mockIcdResponse);
        when(mockIcdResponse.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockIcdResponse.getBody()).thenReturn(mockResponse);
        List<IcdRule> sortedRules = icd10LookupService.getRules(SNOMED_CODE);
        assertNotNull(sortedRules);
        verify(restTemplate, times(2)).exchange(any(), any(), any(), eq(IcdResponse.class));
    }

}