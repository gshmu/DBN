package com.dci.intellij.dbn.execution;

public enum ExecutionType {
    RUN,
    DEBUG,
    DEBUG_JWDP;

    public boolean isDebug() {
        return this == DEBUG || this == DEBUG_JWDP;
    }


}
