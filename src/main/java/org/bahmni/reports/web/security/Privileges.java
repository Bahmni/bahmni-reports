package org.bahmni.reports.web.security;

import java.util.ArrayList;

public class Privileges extends ArrayList<Privilege> {
    boolean hasReportingPrivilege() {
        for (Privilege privilege : this) {
            if (privilege.isReportingPrivilege()) return true;
        }
        return false;
    }
}
