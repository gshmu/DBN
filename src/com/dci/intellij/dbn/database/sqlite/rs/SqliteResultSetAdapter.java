package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.common.util.ResultSetAdapter;

public abstract class SqliteResultSetAdapter extends ResultSetAdapter {
    protected ResultSet childResultSet;
    protected ResultSet parentResultSet;
    protected String parentName;

    public SqliteResultSetAdapter(ResultSet parentResultSet) {
        this.parentResultSet = parentResultSet;
    }

    public SqliteResultSetAdapter(String parentName) {
        this.parentName = parentName;
    }

    public final boolean next() throws SQLException {
        if (parentResultSet != null) {
            if (parentName == null || childResultSet == null || !childResultSet.next()) {
                if (parentResultSet.next()) {
                    parentName = parentResultSet.getString("DATASET_NAME");
                    ConnectionUtil.closeResultSet(childResultSet);
                    childResultSet = loadChildren(parentName);
                    return childResultSet.next() || next();
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            if (childResultSet == null) {
                childResultSet = loadChildren(parentName);
            }
            return childResultSet.next();
        }
    }

    @Override
    public void close() throws SQLException {
        if (parentResultSet!= null) {
            parentResultSet.close();
        }
        if (childResultSet != null) {
            childResultSet.close();
        }
    }

    protected abstract ResultSet loadChildren(String parentName) throws SQLException;
}
