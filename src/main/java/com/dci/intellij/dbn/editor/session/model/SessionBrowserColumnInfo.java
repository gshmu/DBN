package com.dci.intellij.dbn.editor.session.model;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.database.interfaces.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionBrowserColumnInfo extends ResultSetColumnInfo{
    public SessionBrowserColumnInfo(ConnectionHandler connection, ResultSet resultSet, int columnIndex) throws SQLException {
        super(connection, resultSet, columnIndex);
    }

    @Override
    public String translateName(String columnName, ConnectionHandler connection) {
        DatabaseCompatibilityInterface compatibility = connection.getCompatibilityInterface();
        return compatibility.getSessionBrowserColumnName(columnName);
    }

    public SessionBrowserFilterType getFilterType() {
        String name = getName();
        if ("USER".equalsIgnoreCase(name)) return SessionBrowserFilterType.USER;
        if ("HOST".equalsIgnoreCase(name)) return SessionBrowserFilterType.HOST;
        if ("STATUS".equalsIgnoreCase(name)) return SessionBrowserFilterType.STATUS;
        return null;
    }
}
