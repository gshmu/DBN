package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

import java.sql.Driver;
import java.util.List;

public class DriverOption implements Presentable {
    private Driver driver;

    public DriverOption(Driver driver) {
        this.driver = driver;
    }

    public Driver getDriver() {
        return driver;
    }

    @NotNull
    @Override
    public String getName() {
        return driver.getClass().getName();
    }

    public static DriverOption get(List<DriverOption> driverOptions, String name) {
        for (DriverOption driverOption : driverOptions) {
            if (driverOption.getName().equals(name)) {
                return driverOption;
            }
        }
        return null;
    }
}
