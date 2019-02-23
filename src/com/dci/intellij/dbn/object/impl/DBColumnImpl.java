package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationList;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.list.ObjectListProvider;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.properties.DBDataTypePresentableProperty;
import com.dci.intellij.dbn.object.properties.DBObjectPresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.DBObjectType.COLUMN;
import static com.dci.intellij.dbn.object.common.DBObjectType.CONSTRAINT;
import static com.dci.intellij.dbn.object.common.DBObjectType.INDEX;
import static com.dci.intellij.dbn.object.common.DBObjectType.TYPE_ATTRIBUTE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.FOREIGN_KEY;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.HIDDEN;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.NULLABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.PRIMARY_KEY;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.UNIQUE_KEY;

public class DBColumnImpl extends DBObjectImpl implements DBColumn {
    private DBDataType dataType;
    private int position;

    private DBObjectList<DBConstraint> constraints;
    private DBObjectList<DBIndex> indexes;

    DBColumnImpl(@NotNull DBDataset dataset, ResultSet resultSet) throws SQLException {
        super(dataset, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("COLUMN_NAME");
        set(PRIMARY_KEY, "Y".equals(resultSet.getString("IS_PRIMARY_KEY")));
        set(FOREIGN_KEY, "Y".equals(resultSet.getString("IS_FOREIGN_KEY")));
        set(UNIQUE_KEY, "Y".equals(resultSet.getString("IS_UNIQUE_KEY")));
        set(NULLABLE, "Y".equals(resultSet.getString("IS_NULLABLE")));
        set(HIDDEN, "Y".equals(resultSet.getString("IS_HIDDEN")));
        position = resultSet.getInt("POSITION");

        dataType = DBDataType.get(this.getConnectionHandler(), resultSet);
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

    @Override
    public DBObjectType getObjectType() {
        return COLUMN;
    }

    @Override
    public DBDataType getDataType() {
        return dataType;
    }

    @Override
    public int getPosition() {
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
        return indexes.getObjects();
    }

    @Override
    public List<DBConstraint> getConstraints() {
        return constraints.getObjects();
    }

    @Override
    public int getConstraintPosition(DBConstraint constraint) {
        DBObjectRelationListContainer childObjectRelations = getDataset().getChildObjectRelations();
        if (childObjectRelations != null) {
            DBObjectRelationList<DBConstraintColumnRelation> constraintColumnRelations =
                    childObjectRelations.getObjectRelationList(DBObjectRelationType.CONSTRAINT_COLUMN);
            if (constraintColumnRelations != null) {
                for (DBConstraintColumnRelation relation : constraintColumnRelations.getObjectRelations()) {
                    if (relation.getColumn().equals(this) && relation.getConstraint().equals(constraint))
                        return relation.getPosition();
                }
            }
        }
        return 0;
    }

    @Override
    public DBConstraint getConstraintForPosition(int position) {
        DBObjectRelationListContainer childObjectRelations = getDataset().getChildObjectRelations();
        if (childObjectRelations != null) {
            DBObjectRelationList<DBConstraintColumnRelation> constraintColumnRelations =
                    childObjectRelations.getObjectRelationList(DBObjectRelationType.CONSTRAINT_COLUMN);
            if (constraintColumnRelations != null) {
                for (DBConstraintColumnRelation relation : constraintColumnRelations.getObjectRelations()) {
                    if (relation.getColumn().equals(this) && relation.getPosition() == position) {
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
                Integer position = getConstraintPosition(constraint);
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
                    List<DBColumn> columns = null;
                    if (internalColumns != null) {
                        columns = (List<DBColumn>) internalColumns.getObjects();
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
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> navigationLists = new ArrayList<>();

        if (dataType.isDeclared()) {
            navigationLists.add(new DBObjectNavigationListImpl<>("Type", dataType.getDeclaredType()));
        }

        if (constraints.size() > 0) {
            navigationLists.add(new DBObjectNavigationListImpl<>("Constraints", constraints.getObjects()));
        }

        if (getParentObject() instanceof DBTable) {
            if (indexes != null && indexes.size() > 0) {
                navigationLists.add(new DBObjectNavigationListImpl<>("Indexes", indexes.getObjects()));
            }

            if (isForeignKey()) {
                DBColumn foreignKeyColumn = getForeignKeyColumn();
                navigationLists.add(new DBObjectNavigationListImpl<>("Referenced column", foreignKeyColumn));
            }
        }

        if (isPrimaryKey()) {
            ObjectListProvider<DBColumn> objectListProvider = () -> getReferencingColumns();
            navigationLists.add(new DBObjectNavigationListImpl<>("Foreign-key columns", objectListProvider));
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
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
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
