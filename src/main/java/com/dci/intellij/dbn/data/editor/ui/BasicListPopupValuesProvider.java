package com.dci.intellij.dbn.data.editor.ui;

import lombok.Getter;

import java.util.List;

@Getter
public class BasicListPopupValuesProvider extends ListPopupValuesProviderBase {
    private final List<String> values;

    public BasicListPopupValuesProvider(String description, List<String> values) {
        super(description);
        this.values = values;
    }
}
