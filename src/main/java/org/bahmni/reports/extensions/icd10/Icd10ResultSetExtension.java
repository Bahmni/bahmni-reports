package org.bahmni.reports.extensions.icd10;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.extensions.ResultSetExtension;
import org.bahmni.reports.template.TSIntegrationDiagnosisLineReportTemplate;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Map;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.columnStyle;


public class Icd10ResultSetExtension implements ResultSetExtension {
    public static final String ICD_10_COLUMN_NAME = "ICD10 Code(s)";

    public Icd10Evaluator icd10Evaluator = new Icd10Evaluator();

    public void enrich(Collection<Map<String, ?>> collection, JasperReportBuilder jasperReport){
        for (Map<String, ?> rowMap : collection) {
            String terminologyCode = (String) rowMap.get(TSIntegrationDiagnosisLineReportTemplate.TERMINOLOGY_COLUMN_NAME);
            int age = getAgeFromDob((String) rowMap.get(TSIntegrationDiagnosisLineReportTemplate.PATIENT_DATE_OF_BIRTH_COLUMN_NAME));
            String gender = (String) rowMap.get(TSIntegrationDiagnosisLineReportTemplate.GENDER_COLUMN_NAME);
            String icd10Code = icd10Evaluator.getMatchingIcdCodes(terminologyCode, age, gender);
            enrichRow(rowMap, ICD_10_COLUMN_NAME, icd10Code);
        }
        jasperReport.addColumn(col.column(ICD_10_COLUMN_NAME, ICD_10_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
    }

    private int getAgeFromDob(String dateOfBirth) {
        return Period.between(LocalDate.parse(dateOfBirth), LocalDate.now()).getYears();
    }
}
