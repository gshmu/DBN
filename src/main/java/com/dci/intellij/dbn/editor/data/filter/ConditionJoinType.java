package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

public enum ConditionJoinType implements Presentable{
    AND,
    OR;


    @NotNull
    @Override
    public String getName() {
        return name();
    }
}
