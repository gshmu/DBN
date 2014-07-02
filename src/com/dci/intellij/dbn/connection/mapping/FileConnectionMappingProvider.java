package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBSchema;

public interface FileConnectionMappingProvider {
    ConnectionHandler getActiveConnection();
    DBSchema getCurrentSchema();
}
