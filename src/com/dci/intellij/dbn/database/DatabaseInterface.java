package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.connection.ConnectionHandler;

import java.sql.SQLException;

public interface DatabaseInterface {
    ThreadLocal<Cache> META_DATA_CACHE = new ThreadLocal<>();

    SQLException DBN_NOT_CONNECTED_EXCEPTION = new SQLException("Not connected to database");

    void reset();

    static void init(ConnectionHandler connectionHandler) {
        META_DATA_CACHE.set(connectionHandler.getMetaDataCache());
    }

    static void release() {
        META_DATA_CACHE.set(null);
    }

    static Cache getMetaDataCache() {
        return META_DATA_CACHE.get();
    }

    default <T, E extends Throwable> T cached(String key, ThrowableCallable<T, E> loader) throws E{
        return getMetaDataCache().get(key, loader);
    }
}
