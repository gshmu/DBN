package com.dci.intellij.dbn.connection.ssh;

import com.dci.intellij.dbn.common.ui.Presentable;
import lombok.Getter;

@Getter
public enum SshAuthType implements Presentable{
    PASSWORD("Password"),
    KEY_PAIR("Key Pair (Open SSH)");

    SshAuthType(String name) {
        this.name = name;
    }

    private final String name;
}
