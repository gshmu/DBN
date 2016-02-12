package com.dci.intellij.dbn.connection;

public class ConnectionInstructions {
    private boolean allowAutoConnect;
    private boolean allowAutoInit;

    public boolean isAllowAutoConnect() {
        return allowAutoConnect;
    }

    public void setAllowAutoConnect(boolean allowAutoConnect) {
        this.allowAutoConnect = allowAutoConnect;
    }

    public boolean isAllowAutoInit() {
        return allowAutoInit;
    }

    public void setAllowAutoInit(boolean allowAutoInit) {
        this.allowAutoInit = allowAutoInit;
    }
}
