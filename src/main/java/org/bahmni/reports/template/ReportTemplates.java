package org.bahmni.reports.template;

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
