package com.dci.intellij.dbn.connection;

import java.lang.ref.WeakReference;

public class ConnectionHandlerRef extends WeakReference<ConnectionHandler>{
    public ConnectionHandlerRef(ConnectionHandler referent) {
        super(referent);
    }
}
