package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBDatabaseLink;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBDatabaseLinkImpl extends DBSchemaObjectImpl implements DBDatabaseLink {
    private String userName;
    private String host;
    public DBDatabaseLinkImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, DBContentType.NONE, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("DBLINK_NAME");
        userName = resultSet.getString("USER_NAME");
        host = resultSet.getString("HOST");
    }

    @Override
    public void initProperties() {
        getProperties().set(DBObjectProperty.SCHEMA_OBJECT);
    }

    public DBObjectType getObjectType() {
        return DBObjectType.DBLINK;
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.append(true, getHost(), false);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    public String getUserName() {
        return userName;
    }

    public String getHost() {
        return host;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    public boolean isLeafTreeElement() {
        return true;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return BrowserTreeNode.EMPTY_LIST;
    }
}
