package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.database.common.metadata.def.DBNestedTableMetadata;
import com.dci.intellij.dbn.object.DBNestedTable;
import com.dci.intellij.dbn.object.DBNestedTableColumn;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBNestedTableImpl extends DBObjectImpl<DBNestedTableMetadata> implements DBNestedTable {
    private List<DBNestedTableColumn> columns;
    private DBObjectRef<DBType> typeRef;

    DBNestedTableImpl(DBTable parent, DBNestedTableMetadata metadata) throws SQLException {
        super(parent, metadata);

    }

    @Override
    protected String initObject(DBNestedTableMetadata metadata) throws SQLException {
        String name = metadata.getNestedTableName();

        String typeOwner = metadata.getDataTypeOwner();
        String typeName = metadata.getDataTypeName();
        DBSchema schema = getConnectionHandler().getObjectBundle().getSchema(typeOwner);
        typeRef = DBObjectRef.of(schema == null ? null : schema.getType(typeName));
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
