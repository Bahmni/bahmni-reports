package org.bahmni.reports.api.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Arrays;

@Component(value = "dummy-report")
public class DummyReportTemplate implements BaseReportTemplate {

    @Override
    public JasperReportBuilder build(JSONObject reportConfig, String startDate, String endDate) throws SQLException{
        JasperReportBuilder report = DynamicReports.report();

        report.columns(
                Columns.column("Ball Color", "ballColor", DataTypes.stringType()),
                Columns.column("Count", "count", DataTypes.integerType())
                )
                .title(Components.text("Balls in a box").setHorizontalAlignment(HorizontalAlignment.CENTER))
                .setReportName("Dummy Report")
                .setDataSource(Arrays.asList(new DummyData("Red", 10), new DummyData("Blue", 13)));

        return report;
    }

    public class DummyData {
        private String ballColor;
        private Integer count;

        public DummyData(String ballColor, Integer count) {
            this.ballColor = ballColor;
            this.count = count;
        }

        public String getBallColor() {
            return ballColor;
        }

        public Integer getCount() {
            return count;
        }
    }
}
