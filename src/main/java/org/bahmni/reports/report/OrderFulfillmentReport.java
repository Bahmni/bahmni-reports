package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.OrderFulfillmentConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.OrderFulfillmentReportTemplate;

public class OrderFulfillmentReport extends Report<OrderFulfillmentConfig> {

    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new OrderFulfillmentReportTemplate();
    }
}
