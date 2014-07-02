package com.dci.intellij.dbn.object;

public interface DBTypeFunction extends DBFunction {
    public DBType getType();
    int getOverload();
}