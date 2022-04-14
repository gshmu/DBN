package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBFunctionMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBPackageMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBProcedureMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageFunction;
import com.dci.intellij.dbn.object.DBPackageProcedure;
import com.dci.intellij.dbn.object.DBPackageType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public class DBPackageImpl
        extends DBProgramImpl<DBPackageMetadata, DBPackageProcedure, DBPackageFunction>
        implements DBPackage {

    protected DBObjectList<DBPackageType> types;
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
        DBObjectListContainer childObjects = initChildObjects();
        functions = childObjects.createSubcontentObjectList(PACKAGE_FUNCTION, this, schema);
        procedures = childObjects.createSubcontentObjectList(PACKAGE_PROCEDURE, this, schema);
        types = childObjects.createSubcontentObjectList(PACKAGE_TYPE, this, schema);
    }

    @Override
    public List<DBPackageType> getTypes() {
        return types.getObjects();
    }

    @Override
    public DBPackageType getType(String name) {
        return types.getObject(name);
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
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(procedures, functions, types);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        new DynamicSubcontentLoader<DBPackageFunction, DBFunctionMetadata>(PACKAGE, PACKAGE_FUNCTION, true) {

            @Override
            public DynamicContentLoader<DBPackageFunction, DBFunctionMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBPackageFunction, DBFunctionMetadata>(PACKAGE, PACKAGE_FUNCTION, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBPackageFunction> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBPackage packagee = dynamicContent.getParentEntity();
                        return metadataInterface.loadPackageFunctions(
                                getSchemaName(packagee),
                                getObjectName(packagee),
                                connection);
                    }

                    @Override
                    public DBPackageFunction createElement(DynamicContent<DBPackageFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                        DBPackageImpl packagee = content.getParentEntity();
                        return new DBPackageFunctionImpl(packagee, metadata);
                    }
                };
            }

            @Override
            public boolean match(DBPackageFunction function, DynamicContent dynamicContent) {
                DBPackage packagee = dynamicContent.getParentEntity();
                return Commons.match(function.getPackage(), packagee);
            }
        };

        new DynamicSubcontentLoader<DBPackageProcedure, DBProcedureMetadata>(PACKAGE, PACKAGE_PROCEDURE, true) {

            @Override
            public boolean match(DBPackageProcedure procedure, DynamicContent dynamicContent) {
                DBPackage packagee = dynamicContent.getParentEntity();
                return Commons.match(procedure.getPackage(), packagee);
            }

            @Override
            public DynamicContentLoader<DBPackageProcedure, DBProcedureMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBPackageProcedure, DBProcedureMetadata>(PACKAGE, PACKAGE_PROCEDURE, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBPackageProcedure> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBPackage packagee = dynamicContent.getParentEntity();
                        return metadataInterface.loadPackageProcedures(
                                getSchemaName(packagee),
                                getObjectName(packagee),
                                connection);
                    }

                    @Override
                    public DBPackageProcedure createElement(DynamicContent<DBPackageProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                        DBPackageImpl packagee = content.getParentEntity();
                        return new DBPackageProcedureImpl(packagee, metadata);
                    }
                };
            }
        };

        new DynamicSubcontentLoader<DBPackageType, DBTypeMetadata>(PACKAGE, PACKAGE_TYPE, true) {

            @Override
            public boolean match(DBPackageType type, DynamicContent dynamicContent) {
                DBPackage packagee = dynamicContent.getParentEntity();
                return Commons.match(type.getPackage(), packagee);
            }

            @Override
            public DynamicContentLoader<DBPackageType, DBTypeMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBPackageType, DBTypeMetadata>(PACKAGE, PACKAGE_TYPE, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBPackageType> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBPackage packagee = dynamicContent.getParentEntity();
                        return metadataInterface.loadPackageTypes(
                                getSchemaName(packagee),
                                getObjectName(packagee),
                                connection);
                    }

                    @Override
                    public DBPackageType createElement(DynamicContent<DBPackageType> content, DBTypeMetadata metadata, LoaderCache cache) throws SQLException {
                        DBPackageImpl packagee = content.getParentEntity();
                        return new DBPackageTypeImpl(packagee, metadata);
                    }
                };
            }
        };
    }

    @Override
    public String getCodeParseRootId(DBContentType contentType) {
        return contentType == DBContentType.CODE_SPEC ? "package_spec" :
               contentType == DBContentType.CODE_BODY ? "package_body" : null;
    }
}
