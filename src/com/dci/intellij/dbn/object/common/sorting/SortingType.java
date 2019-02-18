package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

public enum SortingType implements Presentable{
    NAME("Name"),
    POSITION("Position");
    private String name;

    SortingType(String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
