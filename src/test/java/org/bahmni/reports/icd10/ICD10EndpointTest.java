package org.bahmni.reports.icd10;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bahmni.reports.extension.icd10.Impl.Icd10LookupServiceImpl;
import org.bahmni.reports.extension.icd10.bean.ICDRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, EntityUtils.class})
public class ICD10EndpointTest {
    @InjectMocks
    Icd10LookupServiceImpl icd10ServiceImpl;

    @Mock
    private CloseableHttpClient mockHttpClient;

    @Mock
    private CloseableHttpResponse mockResponse;

    @Mock
    private StatusLine mockStatusLine;

    @Mock
    private HttpEntity mockHttpEntity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(HttpClients.class, EntityUtils.class);
    }

    @Test
    public void testGetMapRules() throws IOException, URISyntaxException {
        when(HttpClients.createDefault()).thenReturn(mockHttpClient);
        when(mockHttpClient.execute(any())).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
        when(EntityUtils.toString(any())).thenReturn(getMockIcdResponse());

        // Arrange
        String snomedCode = "123456";
        Integer offset = 0;
        Integer limit = 10;
        Boolean termActive = true;

        List<ICDRule> rules = icd10ServiceImpl.getRules(snomedCode, offset, limit, termActive);
        assertEquals(4, rules.size());
    }

    private String getMockIcdResponse() throws URISyntaxException, IOException {
        return readFileAsStr("ts/icd-response2.json");
    }

    private String readFileAsStr(String relativePath) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(relativePath).toURI());
        return Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
    }
}
