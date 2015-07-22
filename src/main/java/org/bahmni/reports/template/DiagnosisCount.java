package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;

@UsingDatasource("openmrs")
public class DiagnosisCount extends BaseReportTemplate<DiagnosisReportConfig> {
    private DiagnosisCountByAgeGroup diagnosisCountByAgeGroup;
    private DiagnosisCountWithoutAgeGroup diagnosisCountWithoutAgeGroup;

    @Autowired
    public DiagnosisCount(DiagnosisCountByAgeGroup diagnosisCountByAgeGroup, DiagnosisCountWithoutAgeGroup diagnosisCountWithoutAgeGroup) {
        this.diagnosisCountByAgeGroup = diagnosisCountByAgeGroup;
        this.diagnosisCountWithoutAgeGroup = diagnosisCountWithoutAgeGroup;
    }

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<DiagnosisReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        super.build(connection, jasperReport, report, startDate, endDate, resources, pageType);

        if (StringUtils.isNotBlank(report.getConfig().getAgeGroupName())){
            return diagnosisCountByAgeGroup.build(connection, jasperReport, report, startDate, endDate, resources);
        }
        return diagnosisCountWithoutAgeGroup.build(connection, jasperReport, report, startDate, endDate, resources);

    }
}
