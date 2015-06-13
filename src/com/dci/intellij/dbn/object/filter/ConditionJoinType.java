package com.dci.intellij.dbn.object.filter;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

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
