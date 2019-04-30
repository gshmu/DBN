package com.dci.intellij.dbn.common.exception;

public class DBUnsupportedOperationException extends UnsupportedOperationException{
    public DBUnsupportedOperationException() {
        super("Operation not supported");
    }
}
