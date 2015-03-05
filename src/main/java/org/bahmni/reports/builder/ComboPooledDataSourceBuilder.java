package org.bahmni.reports.builder;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.stereotype.Component;

import java.beans.PropertyVetoException;
import java.sql.Driver;

@Component
public class ComboPooledDataSourceBuilder {

    private ComboPooledDataSource comboPooledDataSource;

    public ComboPooledDataSourceBuilder() {
        comboPooledDataSource = new ComboPooledDataSource();
    }

    public ComboPooledDataSourceBuilder withUrl(String url) {
        comboPooledDataSource.setJdbcUrl(url);
        return this;
    }

    public ComboPooledDataSourceBuilder withUser(String user) {
        comboPooledDataSource.setUser(user);
        return this;
    }

    public ComboPooledDataSourceBuilder withPassword(String password) {
        comboPooledDataSource.setPassword(password);
        return this;
    }

    public ComboPooledDataSourceBuilder withDriver(Class<? extends Driver> driver) throws PropertyVetoException {
        comboPooledDataSource.setDriverClass(driver.getName());
        return this;
    }


    public ComboPooledDataSource build() {
        comboPooledDataSource.setAcquireIncrement(2);
        comboPooledDataSource.setMinPoolSize(1);
        comboPooledDataSource.setMaxPoolSize(15);
        return comboPooledDataSource;
    }
}
