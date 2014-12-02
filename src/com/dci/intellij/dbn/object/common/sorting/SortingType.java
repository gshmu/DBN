package com.dci.intellij.dbn.object.common.sorting;

public enum SortingType {
    NAME("Name"),
    POSITION("Position");
    private String name;

    SortingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return name;
    }
}
