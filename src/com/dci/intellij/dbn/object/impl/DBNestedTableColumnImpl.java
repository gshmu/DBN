package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
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
        super(parent, resultSet);
        // todo !!!
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        return null; //TODO
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.NESTED_TABLE_COLUMN;
    }

    @Override
    public DBNestedTable getNestedTable() {
        return (DBNestedTable) getParentObject();
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }
}
