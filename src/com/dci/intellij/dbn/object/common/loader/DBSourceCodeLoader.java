package com.dci.intellij.dbn.object.common.loader;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DBSourceCodeLoader {
    protected Logger logger = Logger.getInstance(getClass().getName());

    private DBObject object;
    private boolean lenient;

    protected DBSourceCodeLoader(DBObject object, boolean lenient) {
        this.object = object;
        this.lenient = lenient;
    }

    public String load() throws SQLException {
        DBNConnection connection = null;
        ResultSet resultSet = null;
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        try {
            connection = connectionHandler.getPoolConnection(true);
            resultSet = loadSourceCode(connection);

            StringBuilder sourceCode = new StringBuilder();
            while (resultSet != null && resultSet.next()) {
                String codeLine = resultSet.getString("SOURCE_CODE");
                sourceCode.append(codeLine);
            }

            if (sourceCode.length() == 0 && !lenient)
                throw new SQLException("Source lookup returned empty");

            return StringUtil.removeCharacter(sourceCode.toString(), '\r');
        } finally {
            ConnectionUtil.close(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
    }

    @Nullable
    public abstract ResultSet loadSourceCode(DBNConnection connection) throws SQLException;
}
