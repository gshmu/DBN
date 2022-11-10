package com.dci.intellij.dbn.common.locale;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

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
}
