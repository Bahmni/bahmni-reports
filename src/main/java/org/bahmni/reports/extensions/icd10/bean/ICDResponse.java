package org.bahmni.reports.extensions.icd10.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ICDResponse {
    List<ICDRule> items;

    int total;

    public ICDResponse() {

    }

    public List<ICDRule> getItems() {
        return items;
    }

    public void setItems(List<ICDRule> items) {
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
