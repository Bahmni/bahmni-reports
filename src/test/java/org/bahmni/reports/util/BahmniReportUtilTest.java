package org.bahmni.reports.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BahmniReportUtilTest {

    @Test
    public void shouldPassThroughBothBoundsWhenPresent() {
        assertArrayEquals(new String[]{"2020-01-01", "2020-12-31"},
                BahmniReportUtil.sqlSafeDateRange("2020-01-01", "2020-12-31"));
    }

    @Test
    public void shouldKeepStartAndOpenTheEndWhenEndIsMissing() {
        assertArrayEquals(new String[]{"2020-01-01", "9999-12-31"}, BahmniReportUtil.sqlSafeDateRange("2020-01-01", ""));
        assertArrayEquals(new String[]{"2020-01-01", "9999-12-31"}, BahmniReportUtil.sqlSafeDateRange("2020-01-01", null));
        assertArrayEquals(new String[]{"2020-01-01", "9999-12-31"}, BahmniReportUtil.sqlSafeDateRange("2020-01-01", "null"));
    }

    @Test
    public void shouldKeepEndAndOpenTheStartWhenStartIsMissing() {
        assertArrayEquals(new String[]{"0001-01-01", "2020-12-31"}, BahmniReportUtil.sqlSafeDateRange("", "2020-12-31"));
        assertArrayEquals(new String[]{"0001-01-01", "2020-12-31"}, BahmniReportUtil.sqlSafeDateRange(null, "2020-12-31"));
    }

    @Test
    public void shouldUseAnInvertedRangeWhenBothBoundsAreMissing() {
        assertArrayEquals(new String[]{"9999-12-31", "0001-01-01"}, BahmniReportUtil.sqlSafeDateRange("", " "));
        assertArrayEquals(new String[]{"9999-12-31", "0001-01-01"}, BahmniReportUtil.sqlSafeDateRange(null, null));
    }
}
