package org.bahmni.reports.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LogSanitizerTest {

    @Test
    public void keepsPlainText() {
        assertEquals("/home/bahmni/reports/Visit Report.pdf", LogSanitizer.sanitize("/home/bahmni/reports/Visit Report.pdf"));
    }

    @Test
    public void replacesLineBreaksAndTabs() {
        assertEquals("a_b_c_d", LogSanitizer.sanitize("a\nb\rc\td"));
    }

    // Regression for BAH-5317: the bell and DEL passed through after the explicit-list version replaced the regex.
    @Test
    public void replacesOtherControlCharacters() {
        assertEquals("a_b_c", LogSanitizer.sanitize("a\u0007b\u007Fc"));
    }

    @Test
    public void replacesUnicodeLineSeparators() {
        assertEquals("a_b_c", LogSanitizer.sanitize("a b c"));
    }

    @Test
    public void returnsNullForNull() {
        assertNull(LogSanitizer.sanitize(null));
    }
}
