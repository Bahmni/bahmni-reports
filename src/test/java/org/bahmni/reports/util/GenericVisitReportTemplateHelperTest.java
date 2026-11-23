package org.bahmni.reports.util;

import org.bahmni.reports.model.GenericVisitReportConfig;
import org.junit.Test;

import static org.bahmni.reports.model.GenericVisitReportConfig.DateRange.dateOfAdmission;
import static org.bahmni.reports.model.GenericVisitReportConfig.DateRange.dateOfDischarge;
import static org.bahmni.reports.model.GenericVisitReportConfig.DateRange.visitStartDate;
import static org.bahmni.reports.model.GenericVisitReportConfig.DateRange.visitStopDate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenericVisitReportTemplateHelperTest {

    @Test
    public void shouldReturnTrueOnlyForVisitStopDate() {
        assertTrue(GenericVisitReportTemplateHelper.shouldIncludeOpenVisitsInDateRange(configWith(visitStopDate)));

        assertFalse(GenericVisitReportTemplateHelper.shouldIncludeOpenVisitsInDateRange(configWith(visitStartDate)));
        assertFalse(GenericVisitReportTemplateHelper.shouldIncludeOpenVisitsInDateRange(configWith(dateOfAdmission)));
        assertFalse(GenericVisitReportTemplateHelper.shouldIncludeOpenVisitsInDateRange(configWith(dateOfDischarge)));
    }

    @Test
    public void shouldReturnFalseWhenApplyDateRangeForIsNotSet() {
        assertFalse(GenericVisitReportTemplateHelper.shouldIncludeOpenVisitsInDateRange(configWith(null)));
    }

    @Test
    public void shouldReturnFalseWhenConfigIsNull() {
        assertFalse(GenericVisitReportTemplateHelper.shouldIncludeOpenVisitsInDateRange(null));
    }

    private GenericVisitReportConfig configWith(GenericVisitReportConfig.DateRange dateRange) {
        GenericVisitReportConfig config = new GenericVisitReportConfig();
        config.setApplyDateRangeFor(dateRange);
        return config;
    }
}
