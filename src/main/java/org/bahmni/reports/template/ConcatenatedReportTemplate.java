package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ConcatenatedReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.BahmniReportUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@UsingDatasource("openmrs")
public class ConcatenatedReportTemplate extends BaseReportTemplate<ConcatenatedReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public ConcatenatedReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<ConcatenatedReportConfig> concatenatedReport,
                                     String startDate, String endDate, List<AutoCloseable> resources,
                                     PageType pageType) throws Exception {

        List<Report> subReports = concatenatedReport.getConfig().getReports();
        List<JasperReportBuilder> reportBuilders = new ArrayList<>();

        for (Report subReport : subReports) {
            subReport.setHttpClient(concatenatedReport.getHttpClient());
            BahmniReportBuilder reportBuilder = BahmniReportUtil.build(subReport, connection,
                    startDate, endDate, resources, pageType, bahmniReportsProperties);
            reportBuilders.addAll(reportBuilder.getReportBuilders());
        }
        return new BahmniReportBuilder(reportBuilders.toArray(new JasperReportBuilder[reportBuilders.size()]));
    }
}