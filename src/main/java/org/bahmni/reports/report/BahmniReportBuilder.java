package org.bahmni.reports.report;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BahmniReportBuilder {
  private List<JasperReportBuilder> reportBuilders = new ArrayList<>();

  public BahmniReportBuilder(JasperReportBuilder... builders) {
    this.reportBuilders.addAll(Arrays.asList(builders));
  }

  public List<JasperReportBuilder> getReportBuilders() {
    return reportBuilders;
  }
}
