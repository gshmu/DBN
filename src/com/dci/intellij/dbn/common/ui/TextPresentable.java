package com.dci.intellij.dbn.common.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }
}
