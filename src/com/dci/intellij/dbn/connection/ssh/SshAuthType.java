package com.dci.intellij.dbn.connection.ssh;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum SshAuthType implements Presentable{
    PASSWORD("Password"),
    KEY_PAIR("Key Pair (Open SSH)");

    SshAuthType(String name) {
        this.name = name;
    }

    private String name;

    @NotNull
    @Override
    public String getName() {
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
