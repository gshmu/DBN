package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.object.DBNestedTable;
import com.dci.intellij.dbn.object.DBNestedTableColumn;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBNestedTableImpl extends DBObjectImpl implements DBNestedTable {
    private List<DBNestedTableColumn> columns;
    private DBObjectRef<DBType> typeRef;

    DBNestedTableImpl(DBTable parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);

    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("NESTED_TABLE_NAME");

        String typeOwner = resultSet.getString("DATA_TYPE_OWNER");
        String typeName = resultSet.getString("DATA_TYPE_NAME");
        DBSchema schema = getConnectionHandler().getObjectBundle().getSchema(typeOwner);
        typeRef = DBObjectRef.from(schema == null ? null : schema.getType(typeName));
        // todo !!!
        return name;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.NESTED_TABLE;
    }

    @Override
    public List<DBNestedTableColumn> getColumns() {
        if (columns == null) {
            columns = new ArrayList<DBNestedTableColumn>();
            //todo
        }
        return columns;
    }

    @Override
    public DBNestedTableColumn getColumn(String name) {
        return (DBNestedTableColumn) getObjectByName(getColumns(), name);
    }

    @Override
    public DBTable getTable() {
        return (DBTable) getParentObject();
    }

    public DBType getType() {
        return DBObjectRef.get(typeRef);
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
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
        //return getColumns();
    }
}
