package org.bahmni.reports.web.security;

import java.util.ArrayList;

public class Privilege {
    static final String VIEW_REPORTS_PRIVILEGE = "app:reports";
    private String name;

    String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    boolean isReportingPrivilege() {
        return name.equals(VIEW_REPORTS_PRIVILEGE);
    }
}
