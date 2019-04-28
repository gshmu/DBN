package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.common.util.CommonUtil;
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
        DBObjectListContainer childObjects = initChildObjects();
        columns = childObjects.createSubcontentObjectList(COLUMN, this, schema);
        constraints = childObjects.createSubcontentObjectList(CONSTRAINT, this, schema);
        triggers = childObjects.createSubcontentObjectList(DATASET_TRIGGER, this, schema);

        initChildObjectRelations().createSubcontentObjectRelationList(CONSTRAINT_COLUMN, this, schema);
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
            if (dataType.isNative() && dataType.getNativeDataType().isLargeObject()) {
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
        new DynamicSubcontentLoader<DBColumn, DBColumnMetadata>(DATASET, COLUMN, true) {

            @Override
            public boolean match(DBColumn column, DynamicContent dynamicContent) {
                DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                return CommonUtil.safeEqual(column.getDataset(), dataset);
            }

            @Override
            public DynamicContentLoader<DBColumn, DBColumnMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBColumn, DBColumnMetadata>(DATASET, COLUMN, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBColumn> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                        return metadataInterface.loadColumns(dataset.getSchema().getName(), dataset.getName(), connection);
                    }

                    @Override
                    public DBColumn createElement(DynamicContent<DBColumn> content, DBColumnMetadata metadata, LoaderCache cache) throws SQLException {
                        DBDatasetImpl dataset = (DBDatasetImpl) content.getParentElement();
                        return new DBColumnImpl(dataset, metadata);
                    }
                };
            }
        };

        new DynamicSubcontentLoader<DBConstraint, DBConstraintMetadata>(DATASET, CONSTRAINT, true) {

            @Override
            public boolean match(DBConstraint constraint, DynamicContent dynamicContent) {
                DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                DBDataset constraintDataset = constraint.getDataset();
                return constraintDataset != null && constraintDataset.equals(dataset);
            }

            @Override
            public DynamicContentLoader<DBConstraint, DBConstraintMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBConstraint, DBConstraintMetadata>(DATASET, CONSTRAINT, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBConstraint> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                        return metadataInterface.loadConstraints(dataset.getSchema().getName(), dataset.getName(), connection);
                    }

                    @Override
                    public DBConstraint createElement(DynamicContent<DBConstraint> content, DBConstraintMetadata metadata, LoaderCache cache) throws SQLException {
                        DBDatasetImpl dataset = (DBDatasetImpl) content.getParentElement();
                        return new DBConstraintImpl(dataset, metadata);
                    }
                };

            }
        };

        new DynamicSubcontentLoader<DBDatasetTrigger, DBTriggerMetadata>(DATASET, DATASET_TRIGGER, true) {

            @Override
            public boolean match(DBDatasetTrigger trigger, DynamicContent dynamicContent) {
                DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                return trigger.getDataset().equals(dataset);
            }

            @Override
            public DynamicContentLoader<DBDatasetTrigger, DBTriggerMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBDatasetTrigger, DBTriggerMetadata>(DATASET, DATASET_TRIGGER, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBDatasetTrigger> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                        return metadataInterface.loadDatasetTriggers(dataset.getSchema().getName(), dataset.getName(), connection);
                    }

                    @Override
                    public DBDatasetTrigger createElement(DynamicContent<DBDatasetTrigger> content, DBTriggerMetadata metadata, LoaderCache cache) throws SQLException {
                        DBDataset dataset = (DBDataset) content.getParentElement();
                        return new DBDatasetTriggerImpl(dataset, metadata);
                    }
                };
            }
        };

        new DynamicSubcontentLoader<DBIndex, DBIndexMetadata>(DATASET, INDEX, true) {

            @Override
            public boolean match(DBIndex index, DynamicContent dynamicContent) {
                DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                DBDataset indexDataset = index.getDataset();
                return indexDataset != null && indexDataset.equals(dataset);
            }

            @Override
            public DynamicContentLoader<DBIndex, DBIndexMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBIndex, DBIndexMetadata>(DATASET, INDEX, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                        return metadataInterface.loadIndexes(dataset.getSchema().getName(), dataset.getName(), connection);
                    }

                    @Override
                    public DBIndex createElement(DynamicContent<DBIndex> content, DBIndexMetadata provider, LoaderCache cache) throws SQLException {
                        DBDataset dataset = (DBDataset) content.getParentElement();
                        return new DBIndexImpl(dataset, provider);
                    }
                };
            }
        };

        new DynamicSubcontentLoader(DATASET, INDEX_COLUMN, true) {
            @Override
            public boolean match(DynamicContentElement sourceElement, DynamicContent dynamicContent) {
                DBIndexColumnRelation indexColumnRelation = (DBIndexColumnRelation) sourceElement;
                DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                return indexColumnRelation.getColumn().getDataset().equals(dataset);
            }

            @Override
            public DynamicContentLoader<DBIndexColumnRelation, DBIndexColumnMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBIndexColumnRelation, DBIndexColumnMetadata>(DATASET, INDEX_COLUMN, false, false) {

                    @Override
                    public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                        return metadataInterface.loadIndexRelations(dataset.getSchema().getName(), dataset.getName(), connection);
                    }

                    @Override
                    public DBIndexColumnRelation createElement(DynamicContent<DBIndexColumnRelation> content, DBIndexColumnMetadata metadata, LoaderCache cache) throws SQLException {
                        String columnName = metadata.getColumnName();
                        String indexName = metadata.getIndexName();
                        DBDataset dataset = (DBDataset) content.getParentElement();
                        DBIndex index = dataset.getIndex(indexName);
                        DBColumn column = dataset.getColumn(columnName);

                        if (column != null && index != null) {
                            return new DBIndexColumnRelation(index, column);
                        }
                        return null;
                    }
                };

            }
        };

        new DynamicSubcontentLoader(DATASET, CONSTRAINT_COLUMN, true) {

            @Override
            public boolean match(DynamicContentElement sourceElement, DynamicContent dynamicContent) {
                DBConstraintColumnRelation constraintColumnRelation = (DBConstraintColumnRelation) sourceElement;
                DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                return constraintColumnRelation.getColumn().getDataset() == dataset;
            }

            @Override
            public DynamicContentLoader<DBConstraintColumnRelation, DBConstraintColumnMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBConstraintColumnRelation, DBConstraintColumnMetadata>(DATASET, CONSTRAINT_COLUMN, false, false) {

                    @Override
                    public ResultSet createResultSet(
                            DynamicContent<DBConstraintColumnRelation> dynamicContent,
                            DBNConnection connection) throws SQLException {

                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBDataset dataset = (DBDataset) dynamicContent.getParentElement();
                        return metadataInterface.loadConstraintRelations(dataset.getSchema().getName(), dataset.getName(), connection);
                    }

                    @Override
                    public DBConstraintColumnRelation createElement(
                            DynamicContent<DBConstraintColumnRelation> content,
                            DBConstraintColumnMetadata metadata, LoaderCache cache) throws SQLException {

                        String columnName = metadata.getColumnName();
                        String constraintName = metadata.getConstraintName();
                        int position = metadata.getPosition();

                        DBDataset dataset = (DBDataset) content.getParentElement();
                        DBColumn column = dataset.getColumn(columnName);
                        DBConstraint constraint = dataset.getConstraint(constraintName);

                        if (column != null && constraint != null) {
                            return new DBConstraintColumnRelation(constraint, column, position);
                        }
                        return null;
                    }
                };
            }

        };
    }
}
