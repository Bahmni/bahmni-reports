package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.model.OrderFulfillmentConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static org.bahmni.reports.template.Templates.minimalColumnStyle;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;

@UsingDatasource("openmrs")
public class OrderFulfillmentReportTemplate extends BaseReportTemplate<OrderFulfillmentConfig> {

    private String getFormattedSql(String formattedSql, OrderFulfillmentConfig reportConfig, String startDate, String endDate) {
        ST sqlTemplate = new ST(formattedSql, '#', '#');
        sqlTemplate.add("orderTypes", SqlUtil.toCommaSeparatedSqlString(reportConfig.getOrderTypes()));
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<OrderFulfillmentConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        TextColumnBuilder<String> orderType = col.column("Order Type", "OrderType", type.stringType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<String> concept = col.column("Name", "Concept", type.stringType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<Date> orderDate = col.column("Order Date", "OrderDate", type.dateType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<String> patientId = col.column("Patient ID", "PatientID", type.stringType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<String> patientName = col.column("Patient Name", "PatientName", type.stringType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<String> patientGender = col.column("Gender", "Gender", type.stringType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<String> fulfillmentStatus = col.column("Fulfillment Status", "FulfillmentStatus", type.stringType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        TextColumnBuilder<Date> fulfillmentDate = col.column("Fulfilment Date", "FulfilmentDate", type.dateType())
                .setStyle(minimalColumnStyle)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        String sql = getFileContent("sql/orderFulfillmentReport.sql");

        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        jasperReport.setShowColumnTitle(true)
                .columns(orderType, concept, orderDate, patientId, patientName, patientGender, fulfillmentStatus, fulfillmentDate)
                .setDataSource(getFormattedSql(sql, report.getConfig(), startDate, endDate),
                        connection);
        return new BahmniReportBuilder(jasperReport);
    }
}
