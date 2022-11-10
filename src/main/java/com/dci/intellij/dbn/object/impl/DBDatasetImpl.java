package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBColumnMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBConstraintColumnMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBConstraintMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBIndexColumnMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBIndexMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTriggerMetadata;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBDatasetTrigger;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.type.DBObjectRelationType.CONSTRAINT_COLUMN;
import static com.dci.intellij.dbn.object.type.DBObjectRelationType.INDEX_COLUMN;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public abstract class DBDatasetImpl<M extends DBObjectMetadata> extends DBSchemaObjectImpl<M> implements DBDataset {
    protected DBObjectList<DBColumn> columns;
    protected DBObjectList<DBConstraint> constraints;
    protected DBObjectList<DBDatasetTrigger> triggers;

    DBDatasetImpl(DBSchema parent, M metadata) throws SQLException {
        super(parent, metadata);
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = ensureChildObjects();
        columns = childObjects.createSubcontentObjectList(COLUMN, this, schema);
        constraints = childObjects.createSubcontentObjectList(CONSTRAINT, this, schema);
        triggers = childObjects.createSubcontentObjectList(DATASET_TRIGGER, this, schema);

        childObjects.createSubcontentObjectRelationList(CONSTRAINT_COLUMN, this, schema);
    }

    @Override
    @NotNull
    public List<DBColumn> getColumns() {
        return columns.getObjects();
    }

    @Override
    @Nullable
    public List<DBConstraint> getConstraints() {
        return constraints.getObjects();
    }

    @Override
    @Nullable
    public List<DBDatasetTrigger> getTriggers() {
        return triggers.getObjects();
    }

    @Override
    @Nullable
    public DBColumn getColumn(String name) {
        return columns.getObject(name);
    }

    @Override
    @Nullable
    public DBConstraint getConstraint(String name) {
        return constraints.getObject(name);
    }

    @Override
    @Nullable
    public DBDatasetTrigger getTrigger(String name) {
        return triggers.getObject(name);
    }

    @Nullable
    @Override
    public List<DBIndex> getIndexes() {
        return null;
    }

    @Nullable
    @Override
    public DBIndex getIndex(String name) {
        return null;
    }

    @Override
    public boolean hasLobColumns() {
        for (DBColumn column : getColumns()) {
            DBDataType dataType = column.getDataType();
            if (dataType.isNative() && dataType.getNativeType().isLargeObject()) {
                return true;
            }

        }
        return false;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/

    static {
        DynamicSubcontentLoader.create(DATASET, COLUMN,
                new DynamicContentResultSetLoader<DBColumn, DBColumnMetadata>(DATASET, COLUMN, false, true) {
                    @Override
                    public ResultSet createResultSet(DynamicContent<DBColumn> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = dynamicContent.getParentEntity();
                        return metadataInterface.loadColumns(
                                getSchemaName(dataset),
                                getObjectName(dataset),
                                connection);
                    }

                    @Override
                    public DBColumn createElement(DynamicContent<DBColumn> content, DBColumnMetadata metadata, LoaderCache cache) throws SQLException {
                        DBDataset dataset = content.getParentEntity();
                        return dataset == null ? null : new DBColumnImpl(dataset, metadata);
                    }
                });

        DynamicSubcontentLoader.create(DATASET, CONSTRAINT,
                new DynamicContentResultSetLoader<DBConstraint, DBConstraintMetadata>(DATASET, CONSTRAINT, false, true) {
                    @Override
                    public ResultSet createResultSet(DynamicContent<DBConstraint> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = dynamicContent.getParentEntity();
                        return metadataInterface.loadConstraints(
                                getSchemaName(dataset),
                                getObjectName(dataset),
                                connection);
                    }

                    @Override
                    public DBConstraint createElement(DynamicContent<DBConstraint> content, DBConstraintMetadata metadata, LoaderCache cache) throws SQLException {
                        DBDataset dataset = content.getParentEntity();
                        return new DBConstraintImpl(dataset, metadata);
                    }
                });

        DynamicSubcontentLoader.create(DATASET, DATASET_TRIGGER,
                new DynamicContentResultSetLoader<DBDatasetTrigger, DBTriggerMetadata>(DATASET, DATASET_TRIGGER, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBDatasetTrigger> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = dynamicContent.getParentEntity();
                        return metadataInterface.loadDatasetTriggers(
                                getSchemaName(dataset),
                                getObjectName(dataset),
                                connection);
                    }

                    @Override
                    public DBDatasetTrigger createElement(DynamicContent<DBDatasetTrigger> content, DBTriggerMetadata metadata, LoaderCache cache) throws SQLException {
                        DBDataset dataset = content.getParentEntity();
                        return new DBDatasetTriggerImpl(dataset, metadata);
                    }
                });

        DynamicSubcontentLoader.create(DATASET, INDEX,
                new DynamicContentResultSetLoader<DBIndex, DBIndexMetadata>(DATASET, INDEX, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBIndex> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = dynamicContent.getParentEntity();
                        return metadataInterface.loadIndexes(
                                getSchemaName(dataset),
                                getObjectName(dataset),
                                connection);
                    }

                    @Override
                    public DBIndex createElement(DynamicContent<DBIndex> content, DBIndexMetadata provider, LoaderCache cache) throws SQLException {
                        DBDataset dataset = content.getParentEntity();
                        return new DBIndexImpl(dataset, provider);
                    }
                });


        DynamicSubcontentLoader.create(DATASET, INDEX_COLUMN,
                new DynamicContentResultSetLoader<DBIndexColumnRelation, DBIndexColumnMetadata>(DATASET, INDEX_COLUMN, false, false) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBIndexColumnRelation> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = dynamicContent.getParentEntity();
                        return metadataInterface.loadIndexRelations(
                                getSchemaName(dataset),
                                getObjectName(dataset),
                                connection);
                    }

                    @Override
                    public DBIndexColumnRelation createElement(DynamicContent<DBIndexColumnRelation> content, DBIndexColumnMetadata metadata, LoaderCache cache) throws SQLException {
                        String columnName = metadata.getColumnName();
                        String indexName = metadata.getIndexName();
                        DBDataset dataset = content.getParentEntity();
                        DBIndex index = Safe.call(dataset, d -> d.getIndex(indexName));
                        DBColumn column = Safe.call(dataset, d -> d.getColumn(columnName));

                        if (column != null && index != null) {
                            return new DBIndexColumnRelation(index, column);
                        }
                        return null;
                    }
                });

        DynamicSubcontentLoader.create(DATASET, CONSTRAINT_COLUMN,
                new DynamicContentResultSetLoader<DBConstraintColumnRelation, DBConstraintColumnMetadata>(DATASET, CONSTRAINT_COLUMN, false, false) {

                    @Override
                    public ResultSet createResultSet(
                            DynamicContent<DBConstraintColumnRelation> dynamicContent,
                            DBNConnection connection) throws SQLException {

                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = dynamicContent.getParentEntity();
                        return metadataInterface.loadConstraintRelations(
                                getSchemaName(dataset),
                                getObjectName(dataset),
                                connection);
                    }

                    @Override
                    public DBConstraintColumnRelation createElement(
                            DynamicContent<DBConstraintColumnRelation> content,
                            DBConstraintColumnMetadata metadata, LoaderCache cache) throws SQLException {

                        String columnName = metadata.getColumnName();
                        String constraintName = metadata.getConstraintName();
                        short position = metadata.getPosition();

                        DBDataset dataset = content.getParentEntity();
                        DBColumn column = Safe.call(dataset, d -> d.getColumn(columnName));
                        DBConstraint constraint = Safe.call(dataset, d -> d.getConstraint(constraintName));

                        if (column != null && constraint != null) {
                            return new DBConstraintColumnRelation(constraint, column, position);
                        }
                        return null;
                    }
                });
    }
}
