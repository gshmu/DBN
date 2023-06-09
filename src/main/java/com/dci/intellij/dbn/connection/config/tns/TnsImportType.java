package com.dci.intellij.dbn.connection.config.tns;

import com.dci.intellij.dbn.common.ui.Presentable;
import lombok.Getter;

@Getter
public enum TnsImportType implements Presentable {
    FIELDS("Fields", "Host/port/database"),
    PROFILE("TNS Profile", ""),
    DESCRIPTOR("TNS Descriptor", "");


    private final String name;
    private final String description;

    TnsImportType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
