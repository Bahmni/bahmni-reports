package org.bahmni.reports.builder;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class DateUtil {
    public static Date parseDate(String ymdMaybeHms) {
        try {
            return DateUtils.parseDate(ymdMaybeHms, "yyyy-MM-dd HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
        } catch (ParseException var3) {
            throw new IllegalArgumentException(var3);
        }
    }
}
