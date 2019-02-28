package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBNestedTable;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.DBObjectRelationType.INDEX_COLUMN;
import static com.dci.intellij.dbn.object.common.DBObjectType.*;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.TEMPORARY;

public class DBTableImpl extends DBDatasetImpl implements DBTable {
    private static final List<DBColumn> EMPTY_COLUMN_LIST = new ArrayList<DBColumn>();

    private DBObjectList<DBIndex> indexes;
    private DBObjectList<DBNestedTable> nestedTables;

    DBTableImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("TABLE_NAME");
        set(TEMPORARY, resultSet.getString("IS_TEMPORARY").equals("Y"));
        return name;
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = initChildObjects();
        indexes = childObjects.createSubcontentObjectList(INDEX, this, schema);
        nestedTables = childObjects.createSubcontentObjectList(NESTED_TABLE, this, schema);

        DBObjectRelationListContainer childObjectRelations = initChildObjectRelations();
        childObjectRelations.createSubcontentObjectRelationList(INDEX_COLUMN, this, schema);
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
                    columns = new ArrayList<DBColumn>();
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
                    columns = new ArrayList<DBColumn>();
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
                    columns = new ArrayList<DBColumn>();
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
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = super.createNavigationLists();
        if (indexes.size() > 0) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<DBIndex>("Indexes", indexes.getObjects()));
        }

        return objectNavigationLists;
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
        new DynamicSubcontentLoader<DBNestedTable>(TABLE, NESTED_TABLE, true) {

            @Override
            public boolean match(DBNestedTable nestedTable, DynamicContent dynamicContent) {
                DBTable table = (DBTable) dynamicContent.getParentElement();
                return nestedTable.getTable().equals(table);
            }

            @Override
            public DynamicContentLoader<DBNestedTable> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBNestedTable>(TABLE, NESTED_TABLE, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBNestedTable> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBTable table = (DBTable) dynamicContent.getParentElement();
                        return metadataInterface.loadNestedTables(table.getSchema().getName(), table.getName(), connection);
                    }

                    @Override
                    public DBNestedTable createElement(DynamicContent<DBNestedTable> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                        DBTable table = (DBTable) dynamicContent.getParentElement();
                        return new DBNestedTableImpl(table, resultSet);
                    }
                };
            }
        };
    }
}
