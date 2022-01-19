package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.database.common.metadata.def.DBMaterializedViewMetadata;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBMaterializedView;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.type.DBObjectRelationType.INDEX_COLUMN;

public class DBMaterializedViewImpl extends DBViewImpl implements DBMaterializedView {
    private DBObjectList<DBIndex> indexes;

    DBMaterializedViewImpl(DBSchema schema, DBMaterializedViewMetadata metadata) throws SQLException {
        super(schema, metadata);
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = initChildObjects();
        indexes = childObjects.createSubcontentObjectList(DBObjectType.INDEX, this, schema);

        childObjects.createSubcontentObjectRelationList(INDEX_COLUMN, this, schema);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.MATERIALIZED_VIEW;
    }

    @Override
    @Nullable
    public List<DBIndex> getIndexes() {
        return indexes.getObjects();
    }

    @Override
    @Nullable
    public DBIndex getIndex(String name) {
        return indexes.getObject(name);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(
                columns,
                constraints,
                indexes,
                triggers);
    }
}
