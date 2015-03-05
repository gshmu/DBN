package com.dci.intellij.dbn.data.editor.ui;

import java.util.List;

public class BasicListPopupValuesProvider implements ListPopupValuesProvider{
    private String description;
    private  List<String> values;

    public BasicListPopupValuesProvider(String description, List<String> values) {
        this.values = values;
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public boolean isLazyLoading() {
        return false;
    }
}
