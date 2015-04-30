package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.util.StringUtil;

public enum ConnectionConfigType {
    GENERIC,
    SPECIFIC;

    public static ConnectionConfigType get(String name) {
        if (StringUtil.isNotEmpty(name)) {
            for (ConnectionConfigType configType : values()) {
                if (name.equalsIgnoreCase(configType.name())) return configType;
            }
        }
        return null;
    }


}
