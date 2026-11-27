package org.bahmni.reports.util;

/**
 * Makes request-derived or stored values safe to write to a log line. Control characters and the
 * Unicode line separators are replaced with "_", so a value cannot start a new log entry.
 */
public final class LogSanitizer {

    private static final String CONTROL_OR_LINE_SEPARATOR = "[\\p{Cntrl}\\u2028\\u2029]";

    private LogSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll(CONTROL_OR_LINE_SEPARATOR, "_");
    }
}
