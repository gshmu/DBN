package com.dci.intellij.dbn.editor.session.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterType;

public class SessionBrowserColumnInfo extends ResultSetColumnInfo{
    public SessionBrowserColumnInfo(ConnectionHandler connectionHandler, ResultSet resultSet, int columnIndex) throws SQLException {
        super(connectionHandler, resultSet, columnIndex);
    }

    @Override
    public String translateName(String columnName, ConnectionHandler connectionHandler) {
        DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
        return compatibilityInterface.getSessionBrowserColumnName(columnName);
    }

    public SessionBrowserFilterType getFilterType() {
        switch (getName()) {
            case "USER": return SessionBrowserFilterType.USER;
            case "HOST": return SessionBrowserFilterType.HOST;
            case "STATUS": return SessionBrowserFilterType.STATUS;
        }
        return null;
    }
}
