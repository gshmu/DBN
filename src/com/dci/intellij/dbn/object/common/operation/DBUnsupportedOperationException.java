package com.dci.intellij.dbn.object.common.operation;

import com.dci.intellij.dbn.object.type.DBObjectType;

public class DBUnsupportedOperationException extends UnsupportedOperationException{
    public DBUnsupportedOperationException(DBOperationType operationType, DBObjectType objectType) {
        super( "Operation " + operationType.getName() + " not supported for " + objectType.getListName());
    }

    public DBUnsupportedOperationException(DBOperationType operationType) {
        super( "Operation " + operationType.getName() + " not supported");
    }

    public DBUnsupportedOperationException() {
        super("Operation not supported");
    }
}
