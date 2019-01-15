package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageFunction;
import com.dci.intellij.dbn.object.DBPackageProcedure;
import com.dci.intellij.dbn.object.DBPackageType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.INDEXED;

public class DBPackageImpl extends DBProgramImpl implements DBPackage {
    protected DBObjectList<DBPackageType> types;
    DBPackageImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("PACKAGE_NAME");
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = initChildObjects();
        functions = childObjects.createSubcontentObjectList(DBObjectType.PACKAGE_FUNCTION, this, FUNCTIONS_LOADER, schema);
        procedures = childObjects.createSubcontentObjectList(DBObjectType.PACKAGE_PROCEDURE, this, PROCEDURES_LOADER, schema);
        types = childObjects.createSubcontentObjectList(DBObjectType.PACKAGE_TYPE, this, TYPES_LOADER, schema, INDEXED);
    }

    public List getTypes() {
        return types.getObjects();
    }

    public DBPackageType getType(String name) {
        return types.getObject(name);
    }

    public DBObjectType getObjectType() {
        return DBObjectType.PACKAGE;
    }

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

    public Icon getOriginalIcon() {
        return Icons.DBO_PACKAGE;
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(procedures, functions, types);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/

    private static final DynamicContentLoader<DBPackageFunction> FUNCTIONS_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBPackageFunction>() {
        public ResultSet createResultSet(DynamicContent<DBPackageFunction> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBPackage packagee = (DBPackage) dynamicContent.getParentElement();
            return metadataInterface.loadPackageFunctions(packagee.getSchema().getName(), packagee.getName(), connection);
        }

        public DBPackageFunction createElement(DynamicContent<DBPackageFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBPackageImpl packagee = (DBPackageImpl) dynamicContent.getParentElement();
            return new DBPackageFunctionImpl(packagee, resultSet);
        }
    };

    private static final DynamicSubcontentLoader FUNCTIONS_LOADER = new DynamicSubcontentLoader<DBPackageFunction>(true) {
        public DynamicContentLoader<DBPackageFunction> getAlternativeLoader() {
            return FUNCTIONS_ALTERNATIVE_LOADER;
        }

        public boolean match(DBPackageFunction function, DynamicContent dynamicContent) {
            DBPackage packagee = (DBPackage) dynamicContent.getParentElement();
            return function.getPackage() == packagee;
        }
    };

    private static final DynamicContentLoader<DBPackageProcedure> PROCEDURES_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBPackageProcedure>() {
        public ResultSet createResultSet(DynamicContent<DBPackageProcedure> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBPackage packagee = (DBPackage) dynamicContent.getParentElement();
            return metadataInterface.loadPackageProcedures(packagee.getSchema().getName(), packagee.getName(), connection);
        }

        public DBPackageProcedure createElement(DynamicContent<DBPackageProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBPackageImpl packagee = (DBPackageImpl) dynamicContent.getParentElement();
            return new DBPackageProcedureImpl(packagee, resultSet);
        }
    };

    private static final DynamicSubcontentLoader PROCEDURES_LOADER = new DynamicSubcontentLoader<DBPackageProcedure>(true) {
        public DynamicContentLoader<DBPackageProcedure> getAlternativeLoader() {
            return PROCEDURES_ALTERNATIVE_LOADER;
        }

        public boolean match(DBPackageProcedure procedure, DynamicContent dynamicContent) {
            DBPackage packagee = (DBPackage) dynamicContent.getParentElement();
            return procedure.getPackage() == packagee;
        }
    };

    private static final DynamicContentLoader<DBPackageType> TYPES_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBPackageType>() {
        public ResultSet createResultSet(DynamicContent<DBPackageType> dynamicContent, DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBPackage packagee = (DBPackage) dynamicContent.getParentElement();
            return metadataInterface.loadPackageTypes(packagee.getSchema().getName(), packagee.getName(), connection);
        }

        public DBPackageType createElement(DynamicContent<DBPackageType> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBPackageImpl packagee = (DBPackageImpl) dynamicContent.getParentElement();
            return new DBPackageTypeImpl(packagee, resultSet);
        }
    };

    private static final DynamicSubcontentLoader TYPES_LOADER = new DynamicSubcontentLoader<DBPackageType>(true) {
        public DynamicContentLoader<DBPackageType> getAlternativeLoader() {
            return TYPES_ALTERNATIVE_LOADER;
        }

        public boolean match(DBPackageType type, DynamicContent dynamicContent) {
            DBPackage packagee = (DBPackage) dynamicContent.getParentElement();
            return type.getPackage() == packagee;
        }
    };

    private class SpecSourceCodeLoader extends DBSourceCodeLoader {
        SpecSourceCodeLoader(DBObject object) {
            super(object, false);
        }

        public ResultSet loadSourceCode(DBNConnection connection) throws SQLException {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectSourceCode(
                    getSchema().getName(), getName(), "PACKAGE", connection);
        }
    }

    private class BodySourceCodeLoader extends DBSourceCodeLoader {
        BodySourceCodeLoader(DBObject object) {
            super(object, true);
        }

        public ResultSet loadSourceCode(DBNConnection connection) throws SQLException {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectSourceCode(getSchema().getName(), getName(), "PACKAGE BODY",connection);
        }
    }

    private static DBObjectTimestampLoader SPEC_TIMESTAMP_LOADER = new DBObjectTimestampLoader("PACKAGE") {};
    private static DBObjectTimestampLoader BODY_TIMESTAMP_LOADER = new DBObjectTimestampLoader("PACKAGE BODY") {};

   /*********************************************************
     *                   DBEditableObject                    *
     *********************************************************/
    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
       DBSourceCodeLoader loader =
               contentType == DBContentType.CODE_SPEC ? new SpecSourceCodeLoader(this) :
               contentType == DBContentType.CODE_BODY ? new BodySourceCodeLoader(this) : null;

       return loader == null ? null : loader.load();

    }

    public String getCodeParseRootId(DBContentType contentType) {
        return contentType == DBContentType.CODE_SPEC ? "package_spec" :
               contentType == DBContentType.CODE_BODY ? "package_body" : null;
    }

    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return contentType == DBContentType.CODE_SPEC ? SPEC_TIMESTAMP_LOADER :
               contentType == DBContentType.CODE_BODY ? BODY_TIMESTAMP_LOADER : null;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
