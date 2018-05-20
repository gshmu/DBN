package com.dci.intellij.dbn.common.locale;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public enum DBNumberFormat implements Presentable{
    GROUPED("Grouped"),
    UNGROUPED("Ungrouped"),
    CUSTOM("Custom");

    private String name;

    DBNumberFormat(String name) {
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
