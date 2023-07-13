package org.bahmni.reports.extensions.icd10.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IcdResponse {
    List<IcdRule> items;

    int total;

    public IcdResponse() {

    }

    public List<IcdRule> getItems() {
        return items;
    }

    public void setItems(List<IcdRule> items) {
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
