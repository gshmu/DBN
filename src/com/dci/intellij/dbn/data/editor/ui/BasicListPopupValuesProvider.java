package com.dci.intellij.dbn.data.editor.ui;

import java.util.List;

public class BasicListPopupValuesProvider implements ListPopupValuesProvider{
    private  List<String> values;

    public BasicListPopupValuesProvider(List<String> values) {
        this.values = values;
    }

    @Override
    public List<String> getValues() {
        return values;
    }
}
