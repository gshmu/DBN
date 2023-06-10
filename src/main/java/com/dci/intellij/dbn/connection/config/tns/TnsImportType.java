package com.dci.intellij.dbn.connection.config.tns;

import com.dci.intellij.dbn.common.text.TextContent;
import com.dci.intellij.dbn.common.ui.Presentable;
import lombok.Getter;

@Getter
public enum TnsImportType implements Presentable {
    FIELDS("Fields", TextContent.plain("Basic host/port/database connectivity\nThin JDBC URL based on Host, Port and SID / Service Name\n\nSample: jdbc:oracle:thin:@localhost:1521:XE")),
    PROFILE("TNS Profile", TextContent.plain("TNS wallet connectivity\nThin JDBC URL using TNS_ADMIN wallet location and the TNS profile name\n\nSample: jdbc:oracle:thin:@profile_1?TNS_ADMIN=C:/Users/tns/wallet")),
    DESCRIPTOR("TNS Descriptor", TextContent.plain(""));


    private final String name;
    private final TextContent info;

    TnsImportType(String name, TextContent info) {
        this.name = name;
        this.info = info;
    }
}
