package com.dci.intellij.dbn.database.common;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;

public abstract class DatabaseInterfaceCall<T> extends SimpleTimeoutCall<T> {
    public DatabaseInterfaceCall(long timeout) {
        super(timeout, TimeUnit.SECONDS, null);
    }

    public T execute() throws SQLException {
        return start();
    }

    @Override
    protected T handleException(Exception e) {
        return super.handleException(e);
    }
}
