package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.project.Project;

import java.sql.SQLException;

public interface DatabaseInterface {
    ThreadLocal<Project> PROJECT = new ThreadLocal<Project>();
    ThreadLocal<Cache> META_DATA_CACHE = new ThreadLocal<>();

    SQLException DBN_NOT_CONNECTED_EXCEPTION = new SQLException("Not connected to database");

    void reset();

    static void init(ConnectionHandler connectionHandler) {
        PROJECT.set(connectionHandler.getProject());
        META_DATA_CACHE.set(connectionHandler.getMetaDataCache());
    }

    static void release() {
        PROJECT.set(null);
        META_DATA_CACHE.set(null);
    }

    static Cache getMetaDataCache() {
        return META_DATA_CACHE.get();
    }

    static Project getProject() {
        return PROJECT.get();
    }
}
