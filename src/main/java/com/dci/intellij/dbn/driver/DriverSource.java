package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.ui.Presentable;
import lombok.Getter;

@Getter
public enum DriverSource implements Presentable{
    @Deprecated // replaced by BUNDLED
    BUILTIN("Built-in library"),

    BUNDLED("Bundled library"),
    EXTERNAL("External library");

    DriverSource(String name) {
        this.name = name;
    }

    private final String name;
}
