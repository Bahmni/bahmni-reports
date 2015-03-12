package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.DiagnosisReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Component(value = "diagnosisCount")
@UsingDatasource("openmrs")
public class DiagnosisCount implements BaseReportTemplate<DiagnosisReportConfig> {
    private DiagnosisCountByAgeGroup diagnosisCountByAgeGroup;
    private DiagnosisCountWithoutAgeGroup diagnosisCountWithoutAgeGroup;

    @Autowired
    public DiagnosisCount(DiagnosisCountByAgeGroup diagnosisCountByAgeGroup, DiagnosisCountWithoutAgeGroup diagnosisCountWithoutAgeGroup) {
        this.diagnosisCountByAgeGroup = diagnosisCountByAgeGroup;
        this.diagnosisCountWithoutAgeGroup = diagnosisCountWithoutAgeGroup;
    }

    @Override
    public JasperReportBuilder build(Connection connection, Report<DiagnosisReportConfig> reportConfig, String startDate, String endDate, List<AutoCloseable> resources) throws SQLException, DRException {
        if (StringUtils.isNotBlank(reportConfig.getConfig().getAgeGroupName())){
            return diagnosisCountByAgeGroup.build(connection, reportConfig, startDate, endDate, resources);
        }
        return diagnosisCountWithoutAgeGroup.build(connection, reportConfig, startDate, endDate, resources);

    }
}
