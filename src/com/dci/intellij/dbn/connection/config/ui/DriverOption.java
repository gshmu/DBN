package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import java.sql.Driver;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

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

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
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
