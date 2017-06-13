package org.bahmni.reports.dao;

import org.bahmni.webclients.WebClientsException;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ObservationFormDao {
    ResultSet getResultSet(Connection connection,
                           String startDate, String endDate,
                           List<String> conceptNamesToFilter, List<String> formVersionsList)
            throws SQLException, WebClientsException, InvalidConfigurationException;
}