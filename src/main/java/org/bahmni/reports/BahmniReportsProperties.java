package org.bahmni.reports;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

@Component
public class BahmniReportsProperties {

    protected Properties props;

    public BahmniReportsProperties() {
        this(System.getProperty("user.home") + File.separator + ".bahmni-reports" + File.separator
                + "bahmni-reports.properties");
    }

    public BahmniReportsProperties(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            props = new Properties();
            props.load(new InputStreamReader(inputStream));
            inputStream.close();
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

    public String getOpenmrsTestUrl() {
        return props.getProperty("openmrs.test.url");
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
