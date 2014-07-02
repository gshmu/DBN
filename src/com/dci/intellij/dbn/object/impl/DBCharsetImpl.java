package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.DBContentType;
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
        super(connectionHandler.getObjectBundle(), DBContentType.NONE, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("CHARSET_NAME");
        maxLength = resultSet.getInt("MAX_LENGTH");
    }

    public DBObjectType getObjectType() {
        return DBObjectType.CHARSET;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return BrowserTreeNode.EMPTY_LIST;
    }

    public int getMaxLength() {
        return maxLength;
    }
}
