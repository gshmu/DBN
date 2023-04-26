package com.dci.intellij.dbn.data.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DataModelColumnState {
    private final List<Column> columns = new ArrayList<>();

    @Setter
    @Getter
    public static class Column {
        private String name;
        private boolean visible;

    }
}
