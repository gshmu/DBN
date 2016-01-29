package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.common.util.ResultSetAdapter;

public abstract class SqliteResultSetAdapter extends ResultSetAdapter {
    protected ResultSet childResultSet;
    protected ResultSet parentResultSet;
    protected String parentName;
    private int childCount = 1;
    private int childIndex = -1;

    public SqliteResultSetAdapter(ResultSet parentResultSet) {
        this(parentResultSet, 1);
    }
    public SqliteResultSetAdapter(ResultSet parentResultSet, int childCount) {
        this.parentResultSet = parentResultSet;
        this.childCount = childCount;
    }

    public SqliteResultSetAdapter(String parentName) {
        this(parentName, 1);
    }

    public SqliteResultSetAdapter(String parentName, int childCount) {
        this.parentName = parentName;
        this.childCount = childCount;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public final boolean next() throws SQLException {
        if (parentResultSet != null) {
            if (parentName != null && childNext()) {
                return true;
            } else {
                if (parentResultSet.next()) {
                    parentName = parentResultSet.getString(getParentColumnName());
                    childIndex = -1;
                    return next();
                } else {
                    return false;
                }
            }
        } else {
            return childNext();
        }
    }

    boolean childNext() throws SQLException {
        boolean hasNext = childResultSet != null && !childResultSet.isClosed() && childResultSet.next();
        while (!hasNext && childIndex < childCount -1) {
            childIndex++;
            ConnectionUtil.closeResultSet(childResultSet);
            childResultSet = loadChildren(parentName, childIndex);
            hasNext = childResultSet != null && !childResultSet.isClosed() && childResultSet.next();
        }
        return hasNext;
    }

    protected String getParentColumnName() {
        return "DATASET_NAME";
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

    protected abstract ResultSet loadChildren(String parentName, int index) throws SQLException;
}
