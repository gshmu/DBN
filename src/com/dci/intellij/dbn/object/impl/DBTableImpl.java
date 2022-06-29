package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBNestedTableMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTableMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBNestedTable;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.TEMPORARY;
import static com.dci.intellij.dbn.object.type.DBObjectRelationType.INDEX_COLUMN;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public class DBTableImpl extends DBDatasetImpl<DBTableMetadata> implements DBTable {
    private static final List<DBColumn> EMPTY_COLUMN_LIST = Collections.unmodifiableList(new ArrayList<>());

    private DBObjectList<DBIndex> indexes;
    private DBObjectList<DBNestedTable> nestedTables;

    DBTableImpl(DBSchema schema, DBTableMetadata metadata) throws SQLException {
        super(schema, metadata);
    }

    @Override
    protected String initObject(DBTableMetadata metadata) throws SQLException {
        String name = metadata.getTableName();
        set(TEMPORARY, metadata.isTemporary());
        return name;
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = ensureChildObjects();
        indexes =      childObjects.createSubcontentObjectList(INDEX, this, schema);
        nestedTables = childObjects.createSubcontentObjectList(NESTED_TABLE, this, schema);

        childObjects.createSubcontentObjectRelationList(INDEX_COLUMN, this, schema);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return TABLE;
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return isTemporary() ?
                Icons.DBO_TMP_TABLE :
                Icons.DBO_TABLE;
    }

    @Override
    public boolean isTemporary() {
        return is(TEMPORARY);
    }

    @Override
    @Nullable
    public List<DBIndex> getIndexes() {
        return indexes.getObjects();
    }

    @Override
    public List<DBNestedTable> getNestedTables() {
        return nestedTables.getObjects();
    }

    @Override
    @Nullable
    public DBIndex getIndex(String name) {
        return indexes.getObject(name);
    }

    @Override
    public DBNestedTable getNestedTable(String name) {
        return nestedTables.getObject(name);
    }

    @Override
    public List<DBColumn> getPrimaryKeyColumns() {
        List<DBColumn> columns = null;
        for (DBColumn column : getColumns()) {
            if (column.isPrimaryKey()) {
                if (columns == null) {
                    columns = new ArrayList<>();
                }
                columns.add(column);
            }
        }
        return columns == null ? EMPTY_COLUMN_LIST : columns ;
    }

    @Override
    public List<DBColumn> getForeignKeyColumns() {
        List<DBColumn> columns = null;
        for (DBColumn column : getColumns()) {
            if (column.isForeignKey()) {
                if (columns == null) {
                    columns = new ArrayList<>();
                }
                columns.add(column);
            }
        }
        return columns == null ? EMPTY_COLUMN_LIST : columns ;
    }

    @Override
    public List<DBColumn> getUniqueKeyColumns() {
        List<DBColumn> columns = null;
        for (DBColumn column : getColumns()) {
            if (column.isUniqueKey()) {
                if (columns == null) {
                    columns = new ArrayList<>();
                }
                columns.add(column);
            }
        }
        return columns == null ? EMPTY_COLUMN_LIST : columns ;
    }

    @Override
    public boolean isEditable(DBContentType contentType) {
        return contentType == DBContentType.DATA;
    }


    @Override
    protected @Nullable List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> navigationLists = new LinkedList<>();
        if (indexes.size() > 0) {
            navigationLists.add(DBObjectNavigationList.create("Indexes", indexes.getObjects()));
        }

        return navigationLists;
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
                triggers,
                nestedTables);
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();
        if (isTemporary()) {
            properties.add(0, new SimplePresentableProperty("Attributes", "temporary"));
        }
        return properties;
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        DynamicSubcontentLoader.create(TABLE, NESTED_TABLE,
                new DynamicContentResultSetLoader<DBNestedTable, DBNestedTableMetadata>(TABLE, NESTED_TABLE, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBNestedTable> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBTable table = dynamicContent.getParentEntity();
                        return metadataInterface.loadNestedTables(
                                getSchemaName(table),
                                getObjectName(table),
                                connection);
                    }

                    @Override
                    public DBNestedTable createElement(DynamicContent<DBNestedTable> content, DBNestedTableMetadata metadata, LoaderCache cache) throws SQLException {
                        DBTable table = content.getParentEntity();
                        return new DBNestedTableImpl(table, metadata);
                    }
                });
    }
}
