package com.dci.intellij.dbn.browser.options;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public enum BrowserDisplayMode implements Presentable{

    @Deprecated SINGLE("Single tree"),
    SIMPLE("Single tree"),
    TABBED("Multiple connection tabs");

    private String name;

    BrowserDisplayMode(String name) {
        this.name = name;
    }

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
