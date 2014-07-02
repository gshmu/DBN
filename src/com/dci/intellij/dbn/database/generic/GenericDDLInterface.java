package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseDDLInterfaceImpl;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;

import java.sql.Connection;
import java.sql.SQLException;

public class GenericDDLInterface extends DatabaseDDLInterfaceImpl {
    public GenericDDLInterface(DatabaseInterfaceProvider provider) {
        super("generic_ddl_interface.xml", provider);
    }

    public String getSessionSqlMode(Connection connection) throws SQLException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setSessionSqlMode(String sqlMode, Connection connection) throws SQLException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*********************************************************
     *                   CHANGE statements                   *
     *********************************************************/
    public void updateView(String viewName, String oldCode, String newCode, Connection connection) throws SQLException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void updateTrigger(String tableOwner, String tableName, String triggerName, String oldCode, String newCode, Connection connection) throws SQLException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void updateObject(String objectName, String objectType, String oldCode, String newCode, Connection connection) throws SQLException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*********************************************************
     *                     DROP statements                   *
     *********************************************************/
    private void dropObjectIfExists(String objectType, String objectName, Connection connection) throws SQLException {
        executeQuery(connection, true, "drop-object-if-exists", objectType, objectName);
    }

    /*********************************************************
     *                   CREATE statements                   *
     *********************************************************/
    public void createMethod(MethodFactoryInput method, Connection connection) throws SQLException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
