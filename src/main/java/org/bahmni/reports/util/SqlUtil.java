package org.bahmni.reports.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

public class SqlUtil {
    public static String toCommaSeparatedSqlString(List<String> conceptNames) {
        if (CollectionUtils.isEmpty(conceptNames)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String conceptName : conceptNames) {
            sb.append("'" + StringEscapeUtils.escapeSql(conceptName) + "',");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
