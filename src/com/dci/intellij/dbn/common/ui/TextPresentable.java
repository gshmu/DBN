package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;

public class TextPresentable implements Presentable{
    private String text;
    public TextPresentable(String text) {
        this.text = text;
    }

    @Override
    public String getName() {
        return text;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
