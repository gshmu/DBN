package com.dci.intellij.dbn.object.filter.name;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum ConditionOperator implements Presentable{
    EQUAL("equal", false),
    NOT_EQUAL("not equal", false),
    LIKE("like", true),
    NOT_LIKE("not like", true);

    private String name;
    private boolean allowsWildcards;

    ConditionOperator(String text, boolean allowsWildcards) {
        this.name = text;
        this.allowsWildcards = allowsWildcards;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public boolean allowsWildcards() {
        return allowsWildcards;
    }

    @Override
    public String toString() {
        return name;
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
