package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.database.common.metadata.def.DBColumnMetadata;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.list.*;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.properties.DBDataTypePresentableProperty;
import com.dci.intellij.dbn.object.properties.DBObjectPresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public class DBColumnImpl extends DBObjectImpl<DBColumnMetadata> implements DBColumn {
    private DBDataType dataType;
    private short position;

    private DBObjectList<DBConstraint> constraints;
    private DBObjectList<DBIndex> indexes;

    DBColumnImpl(@NotNull DBDataset dataset, DBColumnMetadata metadata) throws SQLException {
        super(dataset, metadata);
    }

    @Override
    protected String initObject(DBColumnMetadata metadata) throws SQLException {
        String name = metadata.getColumnName();
        set(PRIMARY_KEY, metadata.isPrimaryKey());
        set(FOREIGN_KEY, metadata.isForeignKey());
        set(UNIQUE_KEY, metadata.isUniqueKey());
        set(NULLABLE, metadata.isNullable());
        set(HIDDEN, metadata.isHidden());
        position = metadata.getPosition();

        dataType = DBDataType.get(this.getConnectionHandler(), metadata.getDataType());
        return name;
    }

    @Override
    protected void initLists() {
        DBObjectListContainer childObjects = initChildObjects();
        constraints = childObjects.createSubcontentObjectList(CONSTRAINT, this, getDataset(), DBObjectRelationType.CONSTRAINT_COLUMN);
        indexes = childObjects.createSubcontentObjectList(INDEX, this, getDataset(), DBObjectRelationType.INDEX_COLUMN);

        DBType declaredType = dataType.getDeclaredType();
        if (declaredType != null) {
            DBObjectListContainer typeChildObjects = declaredType.getChildObjects();
            if (typeChildObjects != null) {
                DBObjectList typeAttributes = typeChildObjects.getObjectList(TYPE_ATTRIBUTE);
                childObjects.addObjectList(typeAttributes);
            }
        }
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return COLUMN;
    }

    @Override
    public DBDataType getDataType() {
        return dataType;
    }

    @Override
    public short getPosition() {
        return position;
    }

    @Override
    @Nullable
    public DBObject getDefaultNavigationObject() {
        if (isForeignKey()) {
            return getForeignKeyColumn();
        }
        return null;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.append(false, " - ", true);
        ttb.append(false, dataType.getQualifiedName(), true);

        if (isPrimaryKey()) ttb.append(false,  "&nbsp;&nbsp;PK", true);
        if (isForeignKey()) ttb.append(false, isPrimaryKey() ? ",&nbsp;FK" : "&nbsp;&nbsp;FK", true);
        if (!isPrimaryKey() && !isForeignKey() && !isNullable()) ttb.append(false, "&nbsp;&nbsp;NOT NULL", true);

        if (isForeignKey() && getForeignKeyColumn() != null) {
            ttb.append(true, "FK column:&nbsp;", false);
            DBColumn foreignKeyColumn = getForeignKeyColumn();
            if (foreignKeyColumn != null) {
                ttb.append(false, foreignKeyColumn.getDataset().getName() + '.' + foreignKeyColumn.getName(), false);
            }
        }

        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return isPrimaryKey() ? isForeignKey() ? Icons.DBO_COLUMN_PFK : Icons.DBO_COLUMN_PK :
               isForeignKey() ? Icons.DBO_COLUMN_FK :
               isHidden() ? Icons.DBO_COLUMN_HIDDEN :
               Icons.DBO_COLUMN;
    }

    @Override
    public DBDataset getDataset() {
        return (DBDataset) getParentObject();
    }

    @Override
    public boolean isNullable() {
        return is(NULLABLE);
    }

    @Override
    public boolean isHidden() {
        return is(HIDDEN);
    }

    @Override
    public boolean isPrimaryKey() {
        return is(PRIMARY_KEY);
    }

    @Override
    public boolean isUniqueKey() {
        return is(UNIQUE_KEY);
    }

    @Override
    public boolean isForeignKey() {
        return is(FOREIGN_KEY);
    }

    @Override
    public boolean isSinglePrimaryKey() {
        if (isPrimaryKey()) {
            for (DBConstraint constraint : getConstraints()) {
                if (constraint.isPrimaryKey() && constraint.getColumns().size() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<DBIndex> getIndexes() {
        return indexes == null ? Collections.emptyList() : indexes.getObjects();
    }

    @Override
    public List<DBConstraint> getConstraints() {
        return constraints == null ? Collections.emptyList() : constraints.getObjects();
    }

    @Override
    public short getConstraintPosition(DBConstraint constraint) {
        DBObjectRelationListContainer childObjectRelations = getDataset().getChildObjectRelations();
        if (childObjectRelations != null) {
            DBObjectRelationList<DBConstraintColumnRelation> constraintColumnRelations =
                    childObjectRelations.getObjectRelationList(DBObjectRelationType.CONSTRAINT_COLUMN);
            if (constraintColumnRelations != null) {
                for (DBConstraintColumnRelation relation : constraintColumnRelations.getObjectRelations()) {
                    DBColumn relationColumn = relation.getColumn();
                    DBConstraint relationConstraint = relation.getConstraint();
                    if (relationColumn != null && relationConstraint != null && relationColumn.equals(this) && relationConstraint.equals(constraint))
                        return relation.getPosition();
                }
            }
        }
        return 0;
    }

    @Override
    public DBConstraint getConstraintForPosition(short position) {
        DBObjectRelationListContainer childObjectRelations = getDataset().getChildObjectRelations();
        if (childObjectRelations != null) {
            DBObjectRelationList<DBConstraintColumnRelation> constraintColumnRelations =
                    childObjectRelations.getObjectRelationList(DBObjectRelationType.CONSTRAINT_COLUMN);
            if (constraintColumnRelations != null) {
                for (DBConstraintColumnRelation relation : constraintColumnRelations.getObjectRelations()) {
                    DBColumn relationColumn = relation.getColumn();
                    if (relationColumn != null && relationColumn.equals(this) && relation.getPosition() == position) {
                        return relation.getConstraint();
                    }
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public DBColumn getForeignKeyColumn() {
        for (DBConstraint constraint : getConstraints()) {
            if (constraint.isForeignKey()) {
                short position = getConstraintPosition(constraint);
                DBConstraint foreignKeyConstraint = constraint.getForeignKeyConstraint();
                if (foreignKeyConstraint != null) {
                    return foreignKeyConstraint.getColumnForPosition(position);
                }
            }
        }
        return null;
    }

    @Override
    public List<DBColumn> getReferencingColumns() {
        assert isPrimaryKey();

        List<DBColumn> list = new ArrayList<>();
        boolean isSystemSchema = getDataset().getSchema().isSystemSchema();
        for (DBSchema schema : getConnectionHandler().getObjectBundle().getSchemas()) {
            if (ProgressMonitor.isCancelled()) {
                break;
            }
            if (schema.isSystemSchema() == isSystemSchema) {
                DBObjectListContainer childObjects = schema.getChildObjects();
                if (childObjects != null) {
                    DBObjectList internalColumns = childObjects.getInternalObjectList(COLUMN);
                    if (internalColumns != null) {
                        List<DBColumn> columns = (List<DBColumn>) internalColumns.getObjects();
                        for (DBColumn column : columns){
                            if (this.equals(column.getForeignKeyColumn())) {
                                list.add(column);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    protected @Nullable List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> navigationLists = new LinkedList<>();

        if (dataType.isDeclared()) {
            navigationLists.add(DBObjectNavigationList.create("Type", dataType.getDeclaredType()));
        }

        if (constraints.size() > 0) {
            navigationLists.add(DBObjectNavigationList.create("Constraints", constraints.getObjects()));
        }

        if (getParentObject() instanceof DBTable) {
            if (indexes != null && indexes.size() > 0) {
                navigationLists.add(DBObjectNavigationList.create("Indexes", indexes.getObjects()));
            }

            if (isForeignKey()) {
                DBColumn foreignKeyColumn = getForeignKeyColumn();
                navigationLists.add(DBObjectNavigationList.create("Referenced column", foreignKeyColumn));
            }
        }

        if (isPrimaryKey()) {
            ObjectListProvider<DBColumn> objectListProvider = () -> getReferencingColumns();
            navigationLists.add(DBObjectNavigationList.create("Foreign-key columns", objectListProvider));
        }
        return navigationLists;
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return dataType.getQualifiedName();
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();

        if (isForeignKey()) {
            DBColumn foreignKeyColumn = getForeignKeyColumn();
            if (foreignKeyColumn != null) {
                properties.add(0, new DBObjectPresentableProperty("Foreign key column", foreignKeyColumn, true));
            }
        }

        StringBuilder attributes  = new StringBuilder();
        if (isPrimaryKey()) attributes.append("PK");
        if (isForeignKey()) attributes.append(" FK");
        if (!isPrimaryKey() && !isNullable()) attributes.append(" not null");

        if (attributes.length() > 0) {
            properties.add(0, new SimplePresentableProperty("Attributes", attributes.toString().trim()));
        }
        properties.add(0, new DBDataTypePresentableProperty(dataType));

        return properties;
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        DBObjectListFromRelationListLoader.create(COLUMN, CONSTRAINT);
        DBObjectListFromRelationListLoader.create(COLUMN, INDEX);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBColumn)  {
            DBColumn column = (DBColumn) o;
            if (getDataset().equals(column.getDataset())) {
                if (isPrimaryKey() && column.isPrimaryKey()) {
                    return super.compareTo(o);
                } else if (isPrimaryKey()) {
                    return -1;
                } else if (column.isPrimaryKey()){
                    return 1;
                } else {
                    return super.compareTo(o);
                }
            }
        }
        return super.compareTo(o);
    }
}
