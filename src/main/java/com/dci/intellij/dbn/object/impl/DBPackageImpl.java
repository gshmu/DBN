package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.common.metadata.def.DBFunctionMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBPackageMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBProcedureMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeMetadata;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.filter.type.ObjectTypeFilterSettings;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public class DBPackageImpl
        extends DBProgramImpl<DBPackageMetadata, DBPackageProcedure, DBPackageFunction, DBPackageType>
        implements DBPackage {

    DBPackageImpl(DBSchema schema, DBPackageMetadata metadata) throws SQLException {
        super(schema, metadata);
    }

    @Override
    protected String initObject(DBPackageMetadata metadata) throws SQLException {
        return metadata.getPackageName();
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = ensureChildObjects();
        childObjects.createSubcontentObjectList(PACKAGE_FUNCTION, this, schema);
        childObjects.createSubcontentObjectList(PACKAGE_PROCEDURE, this, schema);
        childObjects.createSubcontentObjectList(PACKAGE_TYPE, this, schema);
    }

    @Override
    protected DBObjectType getFunctionObjectType() {
        return PACKAGE_FUNCTION;
    }

    @Override
    protected DBObjectType getProcedureObjectType() {
        return PACKAGE_PROCEDURE;
    }

    @Override
    protected DBObjectType getTypeObjectType() {
        return PACKAGE_TYPE;
    }

    @Override
    public List<DBPackageType> getTypes() {
        return getChildObjects(PACKAGE_TYPE);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return PACKAGE;
    }

    @Override
    @Nullable
    public Icon getIcon() {
        if (getStatus().is(DBObjectStatus.VALID)) {
            if (getStatus().is(DBObjectStatus.DEBUG))  {
                return Icons.DBO_PACKAGE_DEBUG;
            } else {
                return Icons.DBO_PACKAGE;
            }
        } else {
            return Icons.DBO_PACKAGE_ERR;
        }
    }

    @Override
    public Icon getOriginalIcon() {
        return Icons.DBO_PACKAGE;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @Override
    @NotNull
    public List<BrowserTreeNode> buildPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(
                getChildObjectList(PACKAGE_PROCEDURE),
                getChildObjectList(PACKAGE_FUNCTION),
                getChildObjectList(PACKAGE_TYPE));
    }

    @Override
    public boolean hasVisibleTreeChildren() {
        ObjectTypeFilterSettings settings = getObjectTypeFilterSettings();
        return
            settings.isVisible(PROCEDURE) ||
            settings.isVisible(FUNCTION) ||
            settings.isVisible(TYPE);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        DynamicSubcontentLoader.create(PACKAGE, PACKAGE_FUNCTION,
                new DynamicContentResultSetLoader<DBPackageFunction, DBFunctionMetadata>(PACKAGE, PACKAGE_FUNCTION, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBPackageFunction> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadata = dynamicContent.getMetadataInterface();
                        DBPackage packagee = dynamicContent.getParentEntity();
                        return metadata.loadPackageFunctions(
                                getSchemaName(packagee),
                                getObjectName(packagee),
                                connection);
                    }

                    @Override
                    public DBPackageFunction createElement(DynamicContent<DBPackageFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                        DBPackageImpl packagee = content.getParentEntity();
                        return new DBPackageFunctionImpl(packagee, metadata);
                    }
                });

        DynamicSubcontentLoader.create(PACKAGE, PACKAGE_PROCEDURE,
                new DynamicContentResultSetLoader<DBPackageProcedure, DBProcedureMetadata>(PACKAGE, PACKAGE_PROCEDURE, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBPackageProcedure> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadata = dynamicContent.getMetadataInterface();
                        DBPackage packagee = dynamicContent.getParentEntity();
                        return metadata.loadPackageProcedures(
                                getSchemaName(packagee),
                                getObjectName(packagee),
                                connection);
                    }

                    @Override
                    public DBPackageProcedure createElement(DynamicContent<DBPackageProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                        DBPackageImpl packagee = content.getParentEntity();
                        return new DBPackageProcedureImpl(packagee, metadata);
                    }
                });

        DynamicSubcontentLoader.create(PACKAGE, PACKAGE_TYPE,
                new DynamicContentResultSetLoader<DBPackageType, DBTypeMetadata>(PACKAGE, PACKAGE_TYPE, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBPackageType> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadata = dynamicContent.getMetadataInterface();
                        DBPackage packagee = dynamicContent.getParentEntity();
                        return metadata.loadPackageTypes(
                                getSchemaName(packagee),
                                getObjectName(packagee),
                                connection);
                    }

                    @Override
                    public DBPackageType createElement(DynamicContent<DBPackageType> content, DBTypeMetadata metadata, LoaderCache cache) throws SQLException {
                        DBPackageImpl packagee = content.getParentEntity();
                        return new DBPackageTypeImpl(packagee, metadata);
                    }
                });
    }

    @Override
    public String getCodeParseRootId(DBContentType contentType) {
        return contentType == DBContentType.CODE_SPEC ? "package_spec" :
               contentType == DBContentType.CODE_BODY ? "package_body" : null;
    }
}
