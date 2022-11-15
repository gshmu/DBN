package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.database.interfaces.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaces;

public abstract class DatabaseDebuggerInterfaceImpl extends DatabaseInterfaceBase implements DatabaseDebuggerInterface {
    public DatabaseDebuggerInterfaceImpl(String fileName, DatabaseInterfaces provider) {
        super(fileName, provider);
    }


}
