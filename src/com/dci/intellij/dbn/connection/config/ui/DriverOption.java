package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.ui.Presentable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.Driver;
import java.util.List;
import java.util.Objects;

@Getter
public class DriverOption implements Presentable {
    private final Driver driver;

    public DriverOption(Driver driver) {
        this.driver = driver;
    }

    @NotNull
    @Override
    public String getName() {
        return driver.getClass().getName();
    }

    public static DriverOption get(List<DriverOption> driverOptions, String name) {
        for (DriverOption driverOption : driverOptions) {
            if (Objects.equals(driverOption.getName(), name)) {
                return driverOption;
            }
        }
        return null;
    }
}
