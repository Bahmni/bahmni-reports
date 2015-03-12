package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.UsingDatasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Component(value = "CodedObsByCodedObs")
@UsingDatasource("openmrs")
public class CodedObsByObsReportTemplate implements BaseReportTemplate<Config> {
    @Override
    public JasperReportBuilder build(Connection connection, Report reportConfig, String startDate, String endDate, List resources) throws SQLException, DRException {
        return null;
    }
}
