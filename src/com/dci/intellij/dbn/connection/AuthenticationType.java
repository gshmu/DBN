package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.constant.Constant;
import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

public enum AuthenticationType implements Constant<AuthenticationType>, Presentable {
    NONE("None"),
    USER("User"),
    USER_PASSWORD("User / Password"),
    OS_CREDENTIALS("OS Credentials");

    private String name;

    AuthenticationType(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }
}
