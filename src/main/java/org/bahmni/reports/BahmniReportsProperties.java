package org.bahmni.reports;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("classpath:application.properties")
@Component
public class BahmniReportsProperties {

    @Value("${openelis.url}")
    private String openelisUrl;

    @Value("${openelis.username}")
    private String openelisUser;

    @Value("${openelis.password}")
    private String openelisPassword;

    @Value("${openmrs.url}")
    private String openmrsUrl;

    @Value("${openmrs.username}")
    private String openmrsUser;

    @Value("${openmrs.password}")
    private String openmrsPassword;

    @Value("${config.file.path}")
    private String configFilePath;

    public String getConfigFilePath() {
        return configFilePath;
    }

    public String getOpenelisUrl() {
        return openelisUrl;
    }

    public String getOpenelisUser() {
        return openelisUser;
    }

    public String getOpenelisPassword() {
        return openelisPassword;
    }

    public String getOpenmrsUrl() {
        return openmrsUrl;
    }

    public String getOpenmrsUser() {
        return openmrsUser;
    }

    public String getOpenmrsPassword() {
        return openmrsPassword;
    }
}
