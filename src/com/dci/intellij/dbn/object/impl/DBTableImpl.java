package com.dci.intellij.dbn.object.impl;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;

public class DBTableImpl extends DBDatasetImpl implements DBTable {
    private static final List<DBColumn> EMPTY_COLUMN_LIST = new ArrayList<DBColumn>();

    private DBObjectList<DBIndex> indexes;
    private DBObjectList<DBNestedTable> nestedTables;

    public DBTableImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("TABLE_NAME");
        set(DBObjectProperty.TEMPORARY, resultSet.getString("IS_TEMPORARY").equals("Y"));
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = initChildObjects();
        indexes = childObjects.createSubcontentObjectList(DBObjectType.INDEX, this, INDEXES_LOADER, schema, false);
        nestedTables = childObjects.createSubcontentObjectList(DBObjectType.NESTED_TABLE, this, NESTED_TABLES_LOADER, schema, false);

        DBObjectRelationListContainer childObjectRelations = initChildObjectRelations();
        childObjectRelations.createSubcontentObjectRelationList(DBObjectRelationType.INDEX_COLUMN, this, "Index column relations", INDEX_COLUMN_RELATION_LOADER, schema);
    }

    @Override
    public DBContentType getContentType() {
        return DBContentType.DATA;
    }

    public DBObjectType getObjectType() {
        return DBObjectType.TABLE;
    }

    @Nullable
    public Icon getIcon() {
        return isTemporary() ?
                Icons.DBO_TMP_TABLE :
                Icons.DBO_TABLE;
    }

    public boolean isTemporary() {
        return is(DBObjectProperty.TEMPORARY);
    }

    @Nullable
    public List<DBIndex> getIndexes() {
        return indexes.getObjects();
    }

    public List<DBNestedTable> getNestedTables() {
        return nestedTables.getObjects();
    }

    @Nullable
    public DBIndex getIndex(String name) {
        return indexes.getObject(name);
    }

    public DBNestedTable getNestedTable(String name) {
        return nestedTables.getObject(name);
    }

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

    private static final DynamicSubcontentLoader NESTED_TABLES_LOADER = new DynamicSubcontentLoader<DBNestedTable>(true) {
        public boolean match(DBNestedTable nestedTable, DynamicContent dynamicContent) {
            DBTable table = (DBTable) dynamicContent.getParentElement();
            return nestedTable.getTable().equals(table);
        }

        public DynamicContentLoader<DBNestedTable> getAlternativeLoader() {
            return NESTED_TABLES_ALTERNATIVE_LOADER;
        }
    };

    private static final DynamicContentLoader<DBNestedTable> NESTED_TABLES_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBNestedTable>() {
        public ResultSet createResultSet(DynamicContent<DBNestedTable> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBTable table = (DBTable) dynamicContent.getParentElement();
            return metadataInterface.loadNestedTables(table.getSchema().getName(), table.getName(), connection);
      }

        public DBNestedTable createElement(DynamicContent<DBNestedTable> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBTable table = (DBTable) dynamicContent.getParentElement();
            return new DBNestedTableImpl(table, resultSet);
        }
    };
}
