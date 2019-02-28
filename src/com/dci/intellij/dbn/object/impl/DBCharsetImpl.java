package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBCharset;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBCharsetImpl extends DBObjectImpl implements DBCharset {
    private int maxLength;
    public DBCharsetImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        maxLength = resultSet.getInt("MAX_LENGTH");
        return resultSet.getString("CHARSET_NAME");
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.CHARSET;
    }

    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    @Override
    public int getMaxLength() {
        return maxLength;
    }
}
