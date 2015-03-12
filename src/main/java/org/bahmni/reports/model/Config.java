package org.bahmni.reports.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DiagnosisReportConfig.class, name = "diagnosisReport"),
        @JsonSubTypes.Type(value = SqlReportConfig.class, name = "sqlReport"),
        @JsonSubTypes.Type(value = CodedObsCountConfig.class, name = "codedObsCountReport"),
        @JsonSubTypes.Type(value = Config.class, name = "config"), })
public interface Config {
}
