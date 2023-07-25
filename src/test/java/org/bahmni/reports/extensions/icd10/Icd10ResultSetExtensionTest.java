package org.bahmni.reports.extensions.icd10;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.util.FileReaderUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class Icd10ResultSetExtensionTest {
    @InjectMocks
    Icd10ResultSetExtension icd10ResultSetExtension;
    @Mock
    Icd10Evaluator mockIcd10Evaluator;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddNonBlankValueInEnrichedColumnInResultSet_WhenIcdEvaluatorReturnsNonBlankValue() {
        when(mockIcd10Evaluator.getMatchingIcdCodes(anyString(), anyInt(), anyString())).thenReturn("dummyIcdCode");
        List<Map<String, ?>> collection = getMockDiagnosisResultSetCollection();
        for (Map<String, ?> rowMap : collection) {
            assertEquals(7, rowMap.size());
        }
        icd10ResultSetExtension.enrich(collection, new JasperReportBuilder());
        for (Map<String, ?> rowMap : collection) {
            assertEquals(8, rowMap.size());
            String icd10Value = (String) rowMap.get(Icd10ResultSetExtension.ICD_10_COLUMN_NAME);
            assertTrue(StringUtils.isNotBlank(icd10Value));
            assertEquals("dummyIcdCode", icd10Value);
        }
    }

    @Test
    public void shouldAddEmptyValueInEnrichedColumnInResultSet_WhenIcdEvaluatorReturnsBlankValue() {
        when(mockIcd10Evaluator.getMatchingIcdCodes(anyString(), anyInt(), anyString())).thenReturn(" ");
        List<Map<String, ?>> collection = getMockDiagnosisResultSetCollection();
        for (Map<String, ?> rowMap : collection) {
            assertEquals(7, rowMap.size());
        }
        icd10ResultSetExtension.enrich(collection, new JasperReportBuilder());
        for (Map<String, ?> rowMap : collection) {
            assertEquals(8, rowMap.size());
            String icd10Value = (String) rowMap.get(Icd10ResultSetExtension.ICD_10_COLUMN_NAME);
            assertTrue(StringUtils.isEmpty(icd10Value));
        }
    }

    @Test
    public void shouldAddEmptyValueInEnrichedColumnInResultSet_WhenIcdEvaluatorReturnsEmptyValue() {
        when(mockIcd10Evaluator.getMatchingIcdCodes(anyString(), anyInt(), anyString())).thenReturn("");
        List<Map<String, ?>> collection = getMockDiagnosisResultSetCollection();
        for (Map<String, ?> rowMap : collection) {
            assertEquals(7, rowMap.size());
        }
        icd10ResultSetExtension.enrich(collection, new JasperReportBuilder());
        for (Map<String, ?> rowMap : collection) {
            assertEquals(8, rowMap.size());
            String icd10Value = (String) rowMap.get(Icd10ResultSetExtension.ICD_10_COLUMN_NAME);
            assertTrue(StringUtils.isEmpty(icd10Value));
        }
    }

    @Test
    public void shouldAddEmptyValueInEnrichedColumnInResultSet_WhenIcdEvaluatorReturnsNullValue() {
        when(mockIcd10Evaluator.getMatchingIcdCodes(anyString(), anyInt(), anyString())).thenReturn(null);
        List<Map<String, ?>> collection = getMockDiagnosisResultSetCollection();
        for (Map<String, ?> rowMap : collection) {
            assertEquals(7, rowMap.size());
        }
        icd10ResultSetExtension.enrich(collection, new JasperReportBuilder());
        for (Map<String, ?> rowMap : collection) {
            assertEquals(8, rowMap.size());
            String icd10Value = (String) rowMap.get(Icd10ResultSetExtension.ICD_10_COLUMN_NAME);
            assertTrue(StringUtils.isEmpty(icd10Value));
        }
    }

    private List<Map<String, ?>> getMockDiagnosisResultSetCollection() {
        String resultSetCollectionStr = FileReaderUtil.getFileContent("terminologyServices/diagnosisResultSetCollection.json");
        try {
            return mapper.readValue(resultSetCollectionStr, new TypeReference<List<Map<String, ?>>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}