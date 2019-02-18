package com.dci.intellij.dbn.data.grid.options;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

public enum NullSortingOption implements Presentable{
    FIRST("FIRST"),
    LAST("LAST");

    String name;

    NullSortingOption(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }
}
