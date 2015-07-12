package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionHandler;

public interface DBDebugProcess extends Presentable {
    DBDebugProcessStatus getStatus();

    ConnectionHandler getConnectionHandler();
}
