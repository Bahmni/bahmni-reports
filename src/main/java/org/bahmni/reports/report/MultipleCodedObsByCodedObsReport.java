package org.bahmni.reports.report;

import org.apache.log4j.Logger;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.MultipleCodedObsByCodedObsReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.IpdOpdVisitCount;
import org.bahmni.reports.template.MultipleCodedObsByObsReportTemplate;

public class MultipleCodedObsByCodedObsReport extends Report<MultipleCodedObsByCodedObsReportConfig> {

    private static final Logger logger = Logger.getLogger(MultipleCodedObsByCodedObsReport.class);

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new MultipleCodedObsByObsReportTemplate();
    }
}
