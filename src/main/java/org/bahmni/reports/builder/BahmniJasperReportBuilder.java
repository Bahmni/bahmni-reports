package org.bahmni.reports.builder;

import net.sf.dynamicreports.jasper.base.export.AbstractJasperExporter;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.*;
import net.sf.dynamicreports.jasper.constant.ImageType;
import net.sf.dynamicreports.report.builder.QueryBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;

public class BahmniJasperReportBuilder extends JasperReportBuilder {
    private ResultSet resultSet;
    private Connection connection;
    private String sql;

    public BahmniJasperReportBuilder() {
        super();
    }

    @Override
    public JasperReportBuilder setStartPageNumber(Integer startPageNumber) throws DRException {
        return super.setStartPageNumber(startPageNumber);
    }

    @Override
    public JasperReportBuilder setConnection(Connection connection) {
        return super.setConnection(connection);
    }

    @Override
    public JasperReportBuilder setDataSource(Collection<?> collection) {
        return super.setDataSource(collection);
    }

    @Override
    public JasperReportBuilder setDataSource(ResultSet resultSet) {
        this.resultSet = resultSet;
        return super.setDataSource(resultSet);
    }

    @Override
    public JasperReportBuilder setDataSource(String sql, Connection connection) {
        this.sql = sql;
        this.connection = connection;
        return super.setDataSource(sql, connection);
    }

    @Override
    public JasperReportBuilder setDataSource(QueryBuilder query, Connection connection) {
        return super.setDataSource(query, connection);
    }

    @Override
    public JasperReportBuilder setDataSource(JRDataSource dataSource) {
        return super.setDataSource(dataSource);
    }

    @Override
    public JasperReportBuilder setTemplateDesign(InputStream inputStream) throws DRException {
        return super.setTemplateDesign(inputStream);
    }

    @Override
    public JasperReportBuilder setTemplateDesign(File file) throws DRException {
        return super.setTemplateDesign(file);
    }

    @Override
    public JasperReportBuilder setTemplateDesign(String fileName) throws DRException {
        return super.setTemplateDesign(fileName);
    }

    @Override
    public JasperReportBuilder setTemplateDesign(JasperDesign jasperDesign) throws DRException {
        return super.setTemplateDesign(jasperDesign);
    }

    @Override
    public JasperReportBuilder setTemplateDesign(URL jasperDesignUrl) throws DRException {
        return super.setTemplateDesign(jasperDesignUrl);
    }

    @Override
    public JasperReportBuilder setParameter(String name, Object value) {
        return super.setParameter(name, value);
    }

    @Override
    public JasperReportBuilder setParameters(Map<String, Object> parameters) {
        return super.setParameters(parameters);
    }

    @Override
    public JasperReportBuilder rebuild() throws DRException {
        return super.rebuild();
    }

    @Override
    public JasperDesign toJasperDesign() throws DRException {
        return super.toJasperDesign();
    }

    @Override
    public JasperReport toJasperReport() throws DRException {
        return super.toJasperReport();
    }

    @Override
    public Map<String, Object> getJasperParameters() throws DRException {
        return super.getJasperParameters();
    }

    @Override
    public JasperPrint toJasperPrint() throws DRException {
        return super.toJasperPrint();
    }

    @Override
    public JasperReportBuilder show() throws DRException {
        return super.show();
    }

    @Override
    public JasperReportBuilder show(boolean exitOnClose) throws DRException {
        return super.show(exitOnClose);
    }

    @Override
    public JasperReportBuilder showJrXml() throws DRException {
        return super.showJrXml();
    }

    @Override
    public JasperReportBuilder toJrXml(OutputStream outputStream) throws DRException {
        return super.toJrXml(outputStream);
    }

    @Override
    public JasperReportBuilder print() throws DRException {
        return super.print();
    }

    @Override
    public JasperReportBuilder print(boolean withPrintDialog) throws DRException {
        return super.print(withPrintDialog);
    }

    @Override
    public JasperReportBuilder setVirtualizer(JRVirtualizer virtualizer) {
        return super.setVirtualizer(virtualizer);
    }

    @Override
    public JasperReportBuilder toImage(OutputStream outputStream, ImageType imageType) throws DRException {
        return super.toImage(outputStream, imageType);
    }

    @Override
    public JasperReportBuilder toImage(JasperImageExporterBuilder imageExporterBuilder) throws DRException {
        return super.toImage(imageExporterBuilder);
    }

    @Override
    public JasperReportBuilder toCsv(OutputStream outputStream) throws DRException {
        return super.toCsv(outputStream);
    }

    @Override
    public JasperReportBuilder toCsv(JasperCsvExporterBuilder csvExporterBuilder) throws DRException {
        return super.toCsv(csvExporterBuilder);
    }

    @Override
    public JasperReportBuilder toDocx(OutputStream outputStream) throws DRException {
        return super.toDocx(outputStream);
    }

    @Override
    public JasperReportBuilder toDocx(JasperDocxExporterBuilder docxExporterBuilder) throws DRException {
        return super.toDocx(docxExporterBuilder);
    }

    @Override
    public JasperReportBuilder toHtml(OutputStream outputStream) throws DRException {
        return super.toHtml(outputStream);
    }

    @Override
    public JasperReportBuilder toHtml(JasperHtmlExporterBuilder htmlExporterBuilder) throws DRException {
        return super.toHtml(htmlExporterBuilder);
    }

    @Override
    public JasperReportBuilder toOds(OutputStream outputStream) throws DRException {
        return super.toOds(outputStream);
    }

    @Override
    public JasperReportBuilder toOds(JasperOdsExporterBuilder odsExporterBuilder) throws DRException {
        return super.toOds(odsExporterBuilder);
    }

    @Override
    public JasperReportBuilder toOdt(OutputStream outputStream) throws DRException {
        return super.toOdt(outputStream);
    }

    @Override
    public JasperReportBuilder toOdt(JasperOdtExporterBuilder odtExporterBuilder) throws DRException {
        return super.toOdt(odtExporterBuilder);
    }

    @Override
    public JasperReportBuilder toPdf(OutputStream outputStream) throws DRException {
        return super.toPdf(outputStream);
    }

    @Override
    public JasperReportBuilder toPdf(JasperPdfExporterBuilder pdfExporterBuilder) throws DRException {
        return super.toPdf(pdfExporterBuilder);
    }

    @Override
    public JasperReportBuilder toRtf(OutputStream outputStream) throws DRException {
        return super.toRtf(outputStream);
    }

    @Override
    public JasperReportBuilder toRtf(JasperRtfExporterBuilder rtfExporterBuilder) throws DRException {
        return super.toRtf(rtfExporterBuilder);
    }

    @Override
    public JasperReportBuilder toText(OutputStream outputStream) throws DRException {
        return super.toText(outputStream);
    }

    @Override
    public JasperReportBuilder toText(JasperTextExporterBuilder textExporterBuilder) throws DRException {
        return super.toText(textExporterBuilder);
    }

    @Override
    public JasperReportBuilder toXhtml(OutputStream outputStream) throws DRException {
        return super.toXhtml(outputStream);
    }

    @Override
    public JasperReportBuilder toXhtml(JasperXhtmlExporterBuilder xhtmlExporterBuilder) throws DRException {
        return super.toXhtml(xhtmlExporterBuilder);
    }

    @Override
    public JasperReportBuilder toExcelApiXls(OutputStream outputStream) throws DRException {
        return super.toExcelApiXls(outputStream);
    }

    @Override
    public JasperReportBuilder toExcelApiXls(JasperExcelApiXlsExporterBuilder excelApiXlsExporterBuilder) throws DRException {
        return super.toExcelApiXls(excelApiXlsExporterBuilder);
    }

    @Override
    public JasperReportBuilder toXls(OutputStream outputStream) throws DRException {
        return super.toXls(outputStream);
    }

    @Override
    public JasperReportBuilder toXls(JasperXlsExporterBuilder xlsExporterBuilder) throws DRException {
        return super.toXls(xlsExporterBuilder);
    }

    @Override
    public JasperReportBuilder toXlsx(OutputStream outputStream) throws DRException {
        return super.toXlsx(outputStream);
    }

    @Override
    public JasperReportBuilder toXlsx(JasperXlsxExporterBuilder xlsxExporterBuilder) throws DRException {
        return super.toXlsx(xlsxExporterBuilder);
    }

    @Override
    public JasperReportBuilder toXml(OutputStream outputStream) throws DRException {
        return super.toXml(outputStream);
    }

    @Override
    public JasperReportBuilder toXml(JasperXmlExporterBuilder xmlExporterBuilder) throws DRException {
        return super.toXml(xmlExporterBuilder);
    }

    @Override
    public JasperReportBuilder toPptx(OutputStream outputStream) throws DRException {
        return super.toPptx(outputStream);
    }

    @Override
    public JasperReportBuilder toPptx(JasperPptxExporterBuilder pptxExporterBuilder) throws DRException {
        return super.toPptx(pptxExporterBuilder);
    }

    @Override
    public JasperReportBuilder export(AbstractJasperExporterBuilder<?, ? extends AbstractJasperExporter> exporterBuilder) throws DRException {
        return super.export(exporterBuilder);
    }

    @Override
    public Connection getConnection() {
        return super.getConnection();
    }

    @Override
    public JRDataSource getDataSource() {
        return super.getDataSource();
    }

    public ResultSet getResultSetDataSource() {
        return resultSet;
    }

    public String getSql() {
        return sql;
    }
}
