package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBNestedTable;
import com.dci.intellij.dbn.object.DBNestedTableColumn;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBNestedTableColumnImpl extends DBObjectImpl implements DBNestedTableColumn {

    public DBNestedTableColumnImpl(DBNestedTable parent, ResultSet resultSet) throws SQLException {
        super(parent, DBContentType.NONE, resultSet);
        // todo !!!
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
    }

    public DBObjectType getObjectType() {
        return DBObjectType.NESTED_TABLE_COLUMN;
    }

    public DBNestedTable getNestedTable() {
        return (DBNestedTable) getParentObject();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    public boolean isLeafTreeElement() {
        return true;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return BrowserTreeNode.EMPTY_LIST;
    }
}
