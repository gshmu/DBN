package com.dci.intellij.dbn.connection.ssh;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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
