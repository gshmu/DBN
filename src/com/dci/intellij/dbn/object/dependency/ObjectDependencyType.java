package com.dci.intellij.dbn.object.dependency;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum ObjectDependencyType implements Presentable{
    INCOMING("Incoming references (objects used by this)"),
    OUTGOING("Outgoing references (objects using this)");

    private String name;

    ObjectDependencyType(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }


}
