package org.bahmni.reports.extension.icd10.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.extension.icd10.bean.ICDResponse;
import org.bahmni.reports.extension.icd10.bean.ICDRule;
import org.junit.Assert;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
public class Icd10LookupServiceImplTest {
    @InjectMocks
    Icd10LookupServiceImpl icd10LookupService;
    @Mock
    ResponseEntity<ICDResponse> mockIcdResponse;
    @Mock
    RestTemplate restTemplate;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetSortedIcdRulesWhenInvokedAndResponseCodeIs2xx() {
        String dummyCode = "dummy";
        Integer offset = 0;
        Integer limit = 100;
        Boolean termActive = true;
        ICDResponse mockResponse = getMockMapRules("ts/icd-response2.json");
        when(restTemplate.exchange(any(), any(), any(), eq(ICDResponse.class))).thenReturn(mockIcdResponse);
        when(mockIcdResponse.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockIcdResponse.getBody()).thenReturn(mockResponse);
        List<ICDRule> sortedRules = icd10LookupService.getRules(dummyCode, offset, limit, termActive);
        Assert.assertNotNull(sortedRules);
        Assert.assertEquals(4, sortedRules.size());
        Assert.assertEquals("1", sortedRules.get(0).getMapGroup());
        Assert.assertEquals("1", sortedRules.get(0).getMapPriority());
        Assert.assertEquals("1", sortedRules.get(1).getMapGroup());
        Assert.assertEquals("2", sortedRules.get(1).getMapPriority());
        Assert.assertEquals("2", sortedRules.get(2).getMapGroup());
        Assert.assertEquals("1", sortedRules.get(2).getMapPriority());
        Assert.assertEquals("2", sortedRules.get(3).getMapGroup());
        Assert.assertEquals("2", sortedRules.get(3).getMapPriority());
    }
    @Test
    public void shouldReturnEmptyListWhenInvokedAndResponseCodeIsNot2xx() {
        String dummyCode = "dummy";
        Integer offset = 0;
        Integer limit = 100;
        Boolean termActive = true;
        when(restTemplate.exchange(any(), any(), any(), eq(ICDResponse.class))).thenReturn(mockIcdResponse);
        when(mockIcdResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        List<ICDRule> sortedRules = icd10LookupService.getRules(dummyCode, offset, limit, termActive);
        Assert.assertNotNull(sortedRules);
        Assert.assertEquals(0, sortedRules.size());
    }

    private ICDResponse getMockMapRules(String relativePath) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath)) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, ICDResponse.class);

        } catch (Exception ignored) {

        }
        return new ICDResponse();
    }

}