package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

public enum DatabaseUrlType implements Presentable{
    SID("SID"),
    SERVICE("Service name"),
    LDAP("LDAP"),
    LDAPS("LDAP over SSL"),
    DATABASE("Database"),
    FILE("File");

    private String name;

    DatabaseUrlType(String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }
}
