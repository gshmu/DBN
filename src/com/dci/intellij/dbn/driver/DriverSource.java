package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

public enum DriverSource implements Presentable{
    BUILTIN("Built-in library"),
    EXTERNAL("External library");

    DriverSource(String name) {
        this.name = name;
    }

    private final String name;

    @NotNull
    @Override
    public String getName() {
        return name;
    }
}
