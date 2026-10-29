package org.bahmni.reports.util;

import org.bahmni.reports.model.GenericReportsConfig;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class GenericReportsHelperTest {

    @Test
    public void shouldFallBackToVisitTypeWhenVisitStatusAttributeIsMissing() {
        GenericReportsConfig config = new GenericReportsConfig();
        config.setVisitAttributes(Arrays.asList("Visit Status"));

        String sql = GenericReportsHelper.constructVisitAttributeNamesToDisplay(config);

        assertEquals("COALESCE(GROUP_CONCAT(DISTINCT(IF(vat.name = \\'Visit Status\\', va.value_reference, NULL))), MAX(vt.name)) AS \\'Visit Status\\'", sql);
    }

    @Test
    public void shouldNotApplyFallbackToOtherVisitAttributes() {
        GenericReportsConfig config = new GenericReportsConfig();
        config.setVisitAttributes(Arrays.asList("Admission Status"));

        String sql = GenericReportsHelper.constructVisitAttributeNamesToDisplay(config);

        assertEquals("GROUP_CONCAT(DISTINCT(IF(vat.name = \\'Admission Status\\', va.value_reference, NULL))) AS \\'Admission Status\\'", sql);
    }

    @Test
    public void shouldReturnEmptyStringWhenNoVisitAttributesConfigured() {
        assertEquals("", GenericReportsHelper.constructVisitAttributeNamesToDisplay(new GenericReportsConfig()));
    }
}
