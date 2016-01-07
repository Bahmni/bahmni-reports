package org.bahmni.reports.web.security;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Component
public class OpenMRSAuthenticator {

    private static final Logger logger = Logger.getLogger(OpenMRSAuthenticator.class);
    private static final String WHOAMI_URL = "/bahmnicore/whoami";
    public static final String OPENMRS_SESSION_ID_COOKIE_NAME = "JSESSIONID";

    @Autowired
    private BahmniReportsProperties properties;

    public AuthenticationResponse authenticate(String sessionId) {
        ResponseEntity<Privileges> response = callOpenMRS(sessionId);
        HttpStatus status = response.getStatusCode();

        if (status.series() == HttpStatus.Series.SUCCESSFUL) {
            return response.getBody().hasReportingPrivilege()?
                    AuthenticationResponse.AUTHORIZED:
                    AuthenticationResponse.UNAUTHORIZED;
        }

        return AuthenticationResponse.NOT_AUTHENTICATED;
    }

    public ResponseEntity<Privileges> callOpenMRS(String sessionId) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Cookie", OPENMRS_SESSION_ID_COOKIE_NAME + "=" + sessionId);
        try {
            return new RestTemplate()
                    .exchange(properties.getOpenmrsRootUrl() + WHOAMI_URL,
                            HttpMethod.GET,
                            new HttpEntity<>(null, requestHeaders),
                            Privileges.class
                    );
        } catch (HttpClientErrorException exception) {
            logger.warn("Could not authenticate with OpenMRS", exception);
            return new ResponseEntity<>(exception.getStatusCode());
        }
    }

    private static class Privileges extends ArrayList<Privilege> {
        boolean hasReportingPrivilege() {
            for (Privilege privilege : this) {
                if (privilege.isReportingPrivilege()) return true;
            }
            return false;
        }
    }

    private static class Privilege {
        static final String VIEW_REPORTS_PRIVILEGE = "app:reports";
        private String name;
        private void setName(String name) {
            this.name = name;
        }

        boolean isReportingPrivilege() {
            return name.equals(VIEW_REPORTS_PRIVILEGE);
        }
    }
}
