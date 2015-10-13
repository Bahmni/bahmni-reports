package org.bahmni.reports;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

@Component
public class BahmniReportsProperties {

    Resource resource = new ClassPathResource("/application.properties");
    Properties props;

    public BahmniReportsProperties() {
        try {
            props = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConfigFilePath() {
        return props.getProperty("config.file.path");
    }

    public String getOpenelisUrl() {
        return props.getProperty("openelis.url");
    }

    public String getOpenelisUser() {
        return props.getProperty("openelis.username");
    }

    public String getOpenelisPassword() {
        return props.getProperty("openelis.password");
    }

    public String getOpenmrsUrl() {
        return props.getProperty("openmrs.url");
    }

    public String getOpenmrsUser() {
        return props.getProperty("openmrs.username");
    }

    public String getOpenmrsPassword() {
        return props.getProperty("openmrs.password");
    }

    public String getOpenmrsRootUrl() {
        return props.getProperty("openmrs.service.rootUrl");
    }

    public String getOpenmrsServiceUser() {
        return props.getProperty("openmrs.service.user");
    }

    public String getOpenmrsServicePassword() {
        return props.getProperty("openmrs.service.password");
    }

    public Integer getOpenmrsConnectionTimeout() {
        return Integer.valueOf(props.getProperty("openmrs.connectionTimeoutInMilliseconds"));
    }

    public Integer getOpenmrsReplyTimeout() {
        return Integer.valueOf(props.getProperty("openmrs.replyTimeoutInMilliseconds"));
    }

    public String getMacroTemplatesTempDirectory() {
        return props.getProperty("macrotemplates.temp.directory");
    }
}
