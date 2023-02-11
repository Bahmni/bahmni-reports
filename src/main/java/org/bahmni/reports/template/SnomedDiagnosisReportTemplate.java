package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.*;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.ObsCountConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SnomedDiagnosisReportConfig;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.openmrs.module.fhir2.providers.r3.ValueSetFhirResourceProvider;
import org.stringtemplate.v4.ST;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.ValueSet;








@UsingDatasource("openmrs")
public class SnomedDiagnosisReportTemplate extends BaseReportTemplate<SnomedDiagnosisReportConfig>{

    private final String VISIT_TYPE_CRITERIA = "and va.value_reference in (%s)";
    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<SnomedDiagnosisReportConfig> report, String
            startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {

        StyleBuilder textStyle = stl.style(Templates.columnStyle).setBorder(stl.pen1Point());



        TextColumnBuilder<String> diagnosis = col.column("Diagnosis", "Diagnosis", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<String> snomedCode = col.column("Snomed Code", "SNOMED Code", type.stringType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        TextColumnBuilder<Integer> countTotal = col.column("Count", "Total", type.integerType())
                .setStyle(textStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);



        String sql = getFileContent("sql/snomedCountTest.sql");
        jasperReport.setShowColumnTitle(true)
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
                .setColumnStyle(textStyle)
                .columns(diagnosis,snomedCode, countTotal)
                .setDataSource(getFormattedSql(sql, startDate, endDate , report.getConfig().getSnomedParentCode() ,report.getConfig().getCodes()), connection);

        return new BahmniReportBuilder(jasperReport);
    }

    private String getFormattedSql(String formattedSql, String startDate, String endDate , String snomedParentCode , List<String> codes) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        sqlTemplate.add("snomedParentCode", snomedParentCode);
        sqlTemplate.add("codes", codes);

        return sqlTemplate.render();
    }

    public void fetch(){
        System.out.println(getDescendants("195967001"));
    }
    public List<String> getDescendants(String snomedCode){
        String baseUrl = "https://snowstorm.snomed.mybahmni.in/fhir/";
        String valueSetUrl = "http://snomed.info/sct?fhir_vs=ecl/<<";
        String localeLanguage = "en";
        String valueSetUrlTemplate = "ValueSet/$expand?url={0}{1}&displayLanguage={2}";
        try {
            String relativeUrl = MessageFormat.format(valueSetUrlTemplate, encode(valueSetUrl),snomedCode,localeLanguage);
            String url = baseUrl+relativeUrl;
            //String urlNonEncoded = "https://snowstorm.snomed.mybahmni.in/fhir/ValueSet/$expand?url=http://snomed.info/sct?fhir_vs=ecl/<195967001&displayLanguage=en";
            ValueSet valueSet = FhirContext.forR4().newRestfulGenericClient(baseUrl).read().resource(ValueSet.class).withUrl(url).execute();
            List<String> codes = valueSet.getExpansion().getContains().stream().map(item -> item.getCode()).collect(Collectors.toList());
            return codes;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }


}
