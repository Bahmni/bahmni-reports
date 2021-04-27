package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.reports.model.Config;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.report.BahmniReportBuilder;

import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

public abstract class BaseReportTemplate<T extends Config> {
	private static ResourceBundle localeBundle = null;

	public static class col {
		public static <V> TextColumnBuilder<V> column(String title, String fieldName, Class<V> valueClass) {
			return net.sf.dynamicreports.report.builder.DynamicReports.col.column(title, fieldName, valueClass);
		}

		public static <V> TextColumnBuilder<V> column(String title, String fieldName, DRIDataType<? super V, V> dataType) {
			return Columns.column(getResourceBundleLabel(title), fieldName, dataType);
		}
	}
	
	public void setLocaleBundle(ResourceBundle lBundle) {
		localeBundle = lBundle;
	}

	public ResourceBundle getLocaleBundle() {
		return localeBundle;
	}

    public static String getResourceBundleLabel(String key) {
    	try {
    		String newKey = "";
    		if (!StringUtils.isEmpty(key)) {
    			newKey = key.trim();
    			newKey = newKey.replaceAll(" ", "_");
    		}
	    	if (localeBundle!=null) {
	    		return new String(localeBundle.getString(newKey).getBytes("8859_1"), "UTF-8");
	    	}
    	} catch (Exception e) {
    	}
    	return key;
    }

    public abstract BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<T> report,
                                              String startDate, String endDate, List<AutoCloseable> resources,
                                              PageType pageType) throws Exception;
}