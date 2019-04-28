package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.database.common.metadata.def.DBDatabaseLinkMetadata;
import com.dci.intellij.dbn.object.DBDatabaseLink;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.SCHEMA_OBJECT;

public class DBDatabaseLinkImpl extends DBSchemaObjectImpl<DBDatabaseLinkMetadata> implements DBDatabaseLink {
    private String userName;
    private String host;
    DBDatabaseLinkImpl(DBSchema schema, DBDatabaseLinkMetadata metadata) throws SQLException {
        super(schema, metadata);
    }

    @Override
    protected String initObject(DBDatabaseLinkMetadata metadata) throws SQLException {
        String name = metadata.getDblinkName();
        userName = metadata.getUserName();
        host = metadata.getHost();
        return name;
    }

    @Override
    public void initProperties() {
        properties.set(SCHEMA_OBJECT, true);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.DBLINK;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.append(true, host, false);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getHost() {
        return host;
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
}
