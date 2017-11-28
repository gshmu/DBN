package com.dci.intellij.dbn.object.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBMaterializedView;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;

public class DBMaterializedViewImpl extends DBViewImpl implements DBMaterializedView {
    private DBObjectList<DBIndex> indexes;

    public DBMaterializedViewImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = initChildObjects();
        indexes = childObjects.createSubcontentObjectList(DBObjectType.INDEX, this, INDEXES_LOADER, schema, false);

        DBObjectRelationListContainer childObjectRelations = initChildObjectRelations();
        childObjectRelations.createSubcontentObjectRelationList(DBObjectRelationType.INDEX_COLUMN, this, "Index column relations", INDEX_COLUMN_RELATION_LOADER, schema);
    }

    public DBObjectType getObjectType() {
        return DBObjectType.MATERIALIZED_VIEW;
    }

    @Nullable
    public List<DBIndex> getIndexes() {
        return indexes.getObjects();
    }

    @Nullable
    public DBIndex getIndex(String name) {
        return indexes.getObject(name);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(
                columns,
                constraints,
                indexes,
                triggers);
    }


    /*********************************************************
     *                  DBEditableCodeObject                 *
     ********************************************************/

    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
        SourceCodeLoader loader = new SourceCodeLoader(this);
        return loader.load();
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/

    private class SourceCodeLoader extends DBSourceCodeLoader {
        protected SourceCodeLoader(DBObject object) {
            super(object, false);
        }

        public ResultSet loadSourceCode(DBNConnection connection) throws SQLException {
            return getConnectionHandler().getInterfaceProvider().getMetadataInterface().loadMaterializedViewSourceCode(
                   getSchema().getName(), getName(), connection);
        }
    }
}
