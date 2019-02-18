package com.dci.intellij.dbn.common.ui;

import org.jetbrains.annotations.NotNull;

public class TextPresentable implements Presentable{
    private String text;
    public TextPresentable(String text) {
        this.text = text;
    }

    @NotNull
    @Override
    public String getName() {
        return text;
    }
}
