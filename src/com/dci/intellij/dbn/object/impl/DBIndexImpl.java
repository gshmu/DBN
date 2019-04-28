package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.database.common.metadata.def.DBIndexMetadata;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.object.common.DBObjectType.COLUMN;
import static com.dci.intellij.dbn.object.common.DBObjectType.INDEX;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;

public class DBIndexImpl extends DBSchemaObjectImpl<DBIndexMetadata> implements DBIndex {
    private DBObjectList<DBColumn> columns;

    DBIndexImpl(DBDataset dataset, DBIndexMetadata metadata) throws SQLException {
        super(dataset, metadata);
    }

    @Override
    protected String initObject(DBIndexMetadata metadata) throws SQLException {
        String name = metadata.getIndexName();
        set(UNIQUE, metadata.isUnique());
        return name;
    }

    @Override
    public void initStatus(DBIndexMetadata metadata) throws SQLException {
        boolean valid = metadata.isValid();
        getStatus().set(DBObjectStatus.VALID, valid);
    }

    @Override
    public void initProperties() {
        properties.set(SCHEMA_OBJECT, true);
        properties.set(INVALIDABLE, true);
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBDataset dataset = getDataset();
        if (dataset != null) {
            columns = initChildObjects().createSubcontentObjectList(COLUMN, this, dataset, DBObjectRelationType.INDEX_COLUMN);
        }
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return INDEX;
    }

    @Override
    public DBDataset getDataset() {
        return (DBDataset) getParentObject();
    }

    @Override
    public List<DBColumn> getColumns() {
        return columns == null ? Collections.emptyList() : columns.getObjects();
    }

    @Override
    public boolean isUnique() {
        return is(UNIQUE);
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = super.createNavigationLists();

        if (columns != null && columns.size() > 0) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<>("Columns", columns.getObjects()));
        }
        objectNavigationLists.add(new DBObjectNavigationListImpl<>("Dataset", getDataset()));

        return objectNavigationLists;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /********************************************************
     *                   TreeeElement                       *
     * ******************************************************/

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        DBObjectListFromRelationListLoader.create(INDEX, COLUMN);
    }
}
