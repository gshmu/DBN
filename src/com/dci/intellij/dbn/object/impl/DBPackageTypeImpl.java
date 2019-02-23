package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.common.DBObjectType.PACKAGE_TYPE;
import static com.dci.intellij.dbn.object.common.DBObjectType.TYPE_ATTRIBUTE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.NAVIGABLE;

public class DBPackageTypeImpl extends DBTypeImpl implements DBPackageType {

    DBPackageTypeImpl(DBPackage packagee, ResultSet resultSet) throws SQLException {
        super(packagee, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        return resultSet.getString("TYPE_NAME");
    }

    @Override
    public void initStatus(ResultSet resultSet) {}

    @Override
    public void initProperties() {
        properties.set(NAVIGABLE, true);
    }

    @Override
    protected void initLists() {
        attributes = initChildObjects().createObjectList(TYPE_ATTRIBUTE, this);
    }

    @Override
    public DBPackage getPackage() {
        return (DBPackage) getParentObject();
    }

    @Override
    public DBObjectType getObjectType() {
        return PACKAGE_TYPE;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return isCollection() ? Icons.DBO_TYPE_COLLECTION : Icons.DBO_TYPE;
    }

    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(attributes);
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

    static {
        new DynamicContentResultSetLoader(PACKAGE_TYPE, TYPE_ATTRIBUTE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBPackageTypeImpl type = (DBPackageTypeImpl) dynamicContent.getParentElement();
                return metadataInterface.loadProgramTypeAttributes(
                        type.getSchema().getName(),
                        type.getPackage().getName(),
                        type.getName(), connection);
            }

            @Override
            public DBObject createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                DBTypeImpl type = (DBTypeImpl) dynamicContent.getParentElement();
                return new DBTypeAttributeImpl(type, resultSet);
            }
        };
    }


}
