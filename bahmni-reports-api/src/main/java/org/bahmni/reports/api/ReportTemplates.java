package org.bahmni.reports.api;

import org.bahmni.reports.api.template.BaseReportTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ReportTemplates {

    private ApplicationContext applicationContext;

    @Autowired
    public ReportTemplates(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public BaseReportTemplate get(String name) {
        return (BaseReportTemplate) applicationContext.getBean(name);
    }
    
}
