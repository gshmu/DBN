package com.dci.intellij.dbn.data.grid.options;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum NullSortingOption implements Presentable{
    FIRST("FIRST"),
    LAST("LAST");

    String name;

    NullSortingOption(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
