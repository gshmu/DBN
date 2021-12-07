package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.common.property.PropertyHolderBase;

public class DBDebugProcessStatusHolder extends PropertyHolderBase.IntStore<DBDebugProcessStatus> {
    @Override
    protected DBDebugProcessStatus[] properties() {
        return DBDebugProcessStatus.values();
    }
}
