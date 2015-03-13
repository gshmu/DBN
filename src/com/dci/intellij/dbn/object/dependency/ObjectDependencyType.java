package com.dci.intellij.dbn.object.dependency;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Presentable;

public enum ObjectDependencyType implements Presentable{
    INCOMING("Incoming references (objects used by this)", Icons.DBO_INCOMING_REF, Icons.DBO_INCOMING_REF_SOFT),
    OUTGOING("Outgoing references (objects using this)", Icons.DBO_OUTGOING_REF, Icons.DBO_OUTGOING_REF_SOFT);

    private String name;
    private Icon icon;
    private Icon softIcon;

    ObjectDependencyType(String name, Icon icon, Icon softIcon) {
        this.name = name;
        this.icon = icon;
        this.softIcon = softIcon;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return icon;
    }

    public Icon getSoftIcon() {
        return softIcon;
    }
}
