package org.bahmni.reports.scheduler;

import java.util.Date;

/**
 * This utility class provides an abstraction over time-related operations
 * to make testing easier without relying on PowerMock for static method mocking.
 */
public class ClockUtil {

    /**
     * Returns the current time as a Date object
     */
    public Date now() {
        return new Date();
    }
}
