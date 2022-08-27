package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.constant.Constant;
import com.dci.intellij.dbn.common.ui.Presentable;
import lombok.Getter;

@Getter
public enum AuthenticationType implements Constant<AuthenticationType>, Presentable {
    NONE("None"),
    USER("User"),
    USER_PASSWORD("User / Password"),
    OS_CREDENTIALS("OS Credentials");

    private final String name;

    AuthenticationType(String name) {
        this.name = name;
    }
}
