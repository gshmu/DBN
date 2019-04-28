package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeAttributeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeMetadata;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageType;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;
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

    DBPackageTypeImpl(DBPackage packagee, DBTypeMetadata metadata) throws SQLException {
        super(packagee, metadata);
    }

    @Override
    protected String initObject(DBTypeMetadata metadata) throws SQLException {
        return metadata.getTypeName();
    }

    @Override
    public void initStatus(DBTypeMetadata metadata) {}

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

    @NotNull
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
        new DynamicContentResultSetLoader<DBTypeAttribute, DBTypeAttributeMetadata>(PACKAGE_TYPE, TYPE_ATTRIBUTE, true, true) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                DBPackageType type = (DBPackageType) dynamicContent.getParentElement();
                return metadataInterface.loadProgramTypeAttributes(
                        type.getSchema().getName(),
                        type.getPackage().getName(),
                        type.getName(), connection);
            }

            @Override
            public DBTypeAttribute createElement(DynamicContent<DBTypeAttribute> content, DBTypeAttributeMetadata metadata, LoaderCache cache) throws SQLException {
                DBType type = (DBType) content.getParentElement();
                return new DBTypeAttributeImpl(type, metadata);
            }
        };
    }


}
