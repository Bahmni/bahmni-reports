package org.bahmni.reports.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GlobalPropertyDao {
    public static String getReportUserPassword(Connection connection) throws SQLException {
        try {
            String query = "SELECT property_value FROM global_property WHERE property='reports.user.password';";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getString("property_value");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
