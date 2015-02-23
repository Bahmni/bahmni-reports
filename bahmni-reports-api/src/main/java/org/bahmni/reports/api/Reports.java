package org.bahmni.reports.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Reports {

    private ApplicationContext applicationContext;

    @Autowired
    public Reports(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Report findReport(String name) {
        return (Report) applicationContext.getBean(name);
    }
}
