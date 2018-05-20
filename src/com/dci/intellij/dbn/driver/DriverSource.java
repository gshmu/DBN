package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public enum DriverSource implements Presentable{
    BUILTIN("Built-in library"),
    EXTERNAL("External library");

    DriverSource(String name) {
        this.name = name;
    }

    private String name;

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }


    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }
}
