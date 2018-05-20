package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public enum ConditionJoinType implements Presentable{
    AND,
    OR;


    @NotNull
    @Override
    public String getName() {
        return name();
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
