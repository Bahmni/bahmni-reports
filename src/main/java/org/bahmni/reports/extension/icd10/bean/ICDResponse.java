package org.bahmni.reports.extension.icd10.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ICDResponse {
    List<ICDRule> items;
    public ICDResponse() {

    }

    public List<ICDRule> getItems() {
        return items;
    }

    public void setItems(List<ICDRule> items) {
        this.items = items;
    }
}
