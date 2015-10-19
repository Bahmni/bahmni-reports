package org.bahmni.reports.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.report.*;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.webclients.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        visible = true,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ObsCountReport.class, name = "obsCount"),
        @JsonSubTypes.Type(value = DiagnosisReport.class, name = "diagnosisCount"),
        @JsonSubTypes.Type(value = TestCountReport.class, name = "TestCount"),
        @JsonSubTypes.Type(value = ElisSqlReport.class, name = "ElisGeneric"),
        @JsonSubTypes.Type(value = MRSSqlReport.class, name = "MRSGeneric"),
        @JsonSubTypes.Type(value = IpdOpdVisitCountReport.class, name = "IpdOpdVisitCount"),
        @JsonSubTypes.Type(value = IpdPatientsReport.class, name = "ipdPatients"),
        @JsonSubTypes.Type(value = NumericConceptValuesCountReport.class, name = "NumericConceptValuesCount"),
        @JsonSubTypes.Type(value = ObsCountByConceptClassReport.class, name = "ObsCountByConceptClass"),
        @JsonSubTypes.Type(value = ObsTemplateReport.class, name = "obsTemplate"),
        @JsonSubTypes.Type(value = ObsValueCountReport.class, name = "ObsValueCount"),
        @JsonSubTypes.Type(value = PatientsWithLabtestResultsReport.class, name = "PatientsWithLabtestResults"),
        @JsonSubTypes.Type(value = DateConceptValuesPatientsListReport.class, name = "DateConceptValuesPatientsList"),
        @JsonSubTypes.Type(value = MultipleCodedObsByCodedObsReport.class, name = "MultipleCodedObsByCodedObs"),
        @JsonSubTypes.Type(value = CodedObsByCodedObsReport.class, name = "CodedObsByCodedObs"),
        @JsonSubTypes.Type(value = VisitAggregateCountReport.class, name = "VisitAggregateCount"),
        @JsonSubTypes.Type(value = OrderFulfillmentReport.class, name = "OrderFulfillmentReport"),
        @JsonSubTypes.Type(value = ProgramStateCountReport.class, name = "programStateCount"),
        @JsonSubTypes.Type(value = PatientReport.class, name = "PatientReport"),
        @JsonSubTypes.Type(value = PatientProgramReport.class, name = "PatientProgramReport")
        , @JsonSubTypes.Type(value = ProgramStateTransitionReport.class, name = "programStateTransitionReport")
        , @JsonSubTypes.Type(value = VisitReport.class, name = "visitReport")
})

public abstract class Report<T extends Config> {

    private String name;
    private String type;
    private T config;
    private HttpClient httpClient;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getConfig() {
        return config;
    }

    public void setConfig(T config) {
        this.config = config;
    }

    public abstract BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties);

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
