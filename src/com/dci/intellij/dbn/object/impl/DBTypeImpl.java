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
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.DBTypeFunction;
import com.dci.intellij.dbn.object.DBTypeProcedure;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.DBDataTypePresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.content.DynamicContentStatus.INDEXED;
import static com.dci.intellij.dbn.object.common.DBObjectType.*;

public class DBTypeImpl extends DBProgramImpl implements DBType {
    private static final List<DBTypeAttribute> EMPTY_ATTRIBUTE_LIST = Collections.unmodifiableList(new ArrayList<>(0));

    protected DBObjectList<DBTypeAttribute> attributes;
    protected DBObjectList<DBType> subTypes;

    private String superTypeOwner;
    private String superTypeName;
    private DBObjectRef<DBType> superType;

    private DBDataType.Ref collectionElementTypeRef;
    private DBDataType collectionElementType;

    private DBNativeDataType nativeDataType;

    DBTypeImpl(DBSchemaObject parent, ResultSet resultSet) throws SQLException {
        // type functions are not editable independently
        super(parent, resultSet);
        assert this.getClass() != DBTypeImpl.class;
    }

    DBTypeImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("TYPE_NAME");
        superTypeOwner = resultSet.getString("SUPERTYPE_OWNER");
        superTypeName = resultSet.getString("SUPERTYPE_NAME");

        String typecode = resultSet.getString("TYPECODE");
        set(DBObjectProperty.COLLECTION, "COLLECTION".equals(typecode));
        ConnectionHandler connectionHandler = getConnectionHandler();
        nativeDataType = connectionHandler.getObjectBundle().getNativeDataType(typecode);
        if (isCollection()) {
            collectionElementTypeRef = new DBDataType.Ref(resultSet,  "COLLECTION_");
        }
    }

    protected void initLists() {
        super.initLists();
        if (!isCollection()) {
            DBObjectListContainer container = initChildObjects();
            DBSchema schema = getSchema();
            attributes = container.createSubcontentObjectList(TYPE_ATTRIBUTE, this, schema, INDEXED);
            procedures = container.createSubcontentObjectList(TYPE_PROCEDURE, this, schema);
            functions = container.createSubcontentObjectList(TYPE_FUNCTION, this, schema);
            subTypes = container.createSubcontentObjectList(TYPE, this, schema, INDEXED);
        }
    }

    public DBObjectType getObjectType() {
        return TYPE;
    }

    @Nullable
    public Icon getIcon() {
        if (getStatus().is(DBObjectStatus.VALID)) {
            if (getStatus().is(DBObjectStatus.DEBUG))  {
                return Icons.DBO_TYPE_DEBUG;
            } else {
                return isCollection() ? Icons.DBO_TYPE_COLLECTION : Icons.DBO_TYPE;
            }
        } else {
            return isCollection() ? Icons.DBO_TYPE_COLLECTION_ERR : Icons.DBO_TYPE_ERR;
        }
    }

    public Icon getOriginalIcon() {
        return isCollection() ? Icons.DBO_TYPE_COLLECTION : Icons.DBO_TYPE;
    }

    public List<DBTypeAttribute> getAttributes() {
        return attributes == null ? EMPTY_ATTRIBUTE_LIST : attributes.getObjects();
    }

    public DBType getSuperType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (superType == null && superTypeOwner != null && superTypeName != null) {
            DBType type = connectionHandler.getObjectBundle().getSchema(superTypeOwner).getType(superTypeName);
            superType = DBObjectRef.from(type);
            superTypeOwner = null;
            superTypeName = null;
        }
        return DBObjectRef.get(superType);
    }

    public DBDataType getCollectionElementType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (collectionElementType == null && collectionElementTypeRef != null) {
            collectionElementType = collectionElementTypeRef.get(connectionHandler);
            collectionElementTypeRef = null;
        }
        return collectionElementType;
    }

    @Nullable
    public DBObject getDefaultNavigationObject() {
        if (isCollection()) {
            DBDataType dataType = getCollectionElementType();
            if (dataType != null && dataType.isDeclared()) {
                return dataType.getDeclaredType();
            }

        }
        return null;
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();
        DBDataType collectionElementType = getCollectionElementType();
        if (collectionElementType != null) {
            properties.add(0, new DBDataTypePresentableProperty("Collection element type", collectionElementType));
        }
        return properties;
    }

    public List<DBType> getSubTypes() {
        return subTypes.getObjects();
    }

    public DBNativeDataType getNativeDataType() {
        return nativeDataType;
    }

    public boolean isCollection() {
        return is(DBObjectProperty.COLLECTION);
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
        return isCollection() ?
                EMPTY_TREE_NODE_LIST :
                DatabaseBrowserUtils.createList(attributes, procedures, functions);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        new DynamicSubcontentLoader<DBTypeAttribute>(TYPE, TYPE_ATTRIBUTE, true) {

            public boolean match(DBTypeAttribute typeAttribute, DynamicContent dynamicContent) {
                DBType type = (DBType) dynamicContent.getParentElement();
                return typeAttribute.getType().equals(type);
            }

            public DynamicContentLoader<DBTypeAttribute> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBTypeAttribute>(TYPE, TYPE_ATTRIBUTE, false) {

                    public ResultSet createResultSet(DynamicContent<DBTypeAttribute> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                        DBType type = (DBType) dynamicContent.getParentElement();
                        return metadataInterface.loadTypeAttributes(type.getSchema().getName(), type.getName(), connection);
                    }

                    public DBTypeAttribute createElement(DynamicContent<DBTypeAttribute> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                        DBTypeImpl type = (DBTypeImpl) dynamicContent.getParentElement();
                        return new DBTypeAttributeImpl(type, resultSet);
                    }
                };

            }
        };

        new DynamicSubcontentLoader<DBTypeFunction>(TYPE, TYPE_FUNCTION, true) {

            public boolean match(DBTypeFunction function, DynamicContent dynamicContent) {
                DBType type = (DBType) dynamicContent.getParentElement();
                return function.getType() == type;
            }

            public DynamicContentLoader<DBTypeFunction> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBTypeFunction>(TYPE, TYPE_FUNCTION, false) {

                    public ResultSet createResultSet(DynamicContent<DBTypeFunction> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                        DBType type = (DBType) dynamicContent.getParentElement();
                        return metadataInterface.loadTypeFunctions(type.getSchema().getName(), type.getName(), connection);
                    }

                    public DBTypeFunction createElement(DynamicContent<DBTypeFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                        DBType type = (DBType) dynamicContent.getParentElement();
                        return new DBTypeFunctionImpl(type, resultSet);
                    }
                };
            }
        };

        new DynamicSubcontentLoader<DBTypeProcedure>(TYPE, TYPE_PROCEDURE, true) {

            public boolean match(DBTypeProcedure procedure, DynamicContent dynamicContent) {
                DBType type = (DBType) dynamicContent.getParentElement();
                return procedure.getType() == type;
            }

            public DynamicContentLoader<DBTypeProcedure> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBTypeProcedure>(TYPE, TYPE_PROCEDURE, false) {

                    public ResultSet createResultSet(DynamicContent<DBTypeProcedure> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
                        DBType type = (DBType) dynamicContent.getParentElement();
                        return metadataInterface.loadTypeProcedures(type.getSchema().getName(), type.getName(), connection);
                    }

                    public DBTypeProcedure createElement(DynamicContent<DBTypeProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
                        DBType type = (DBType) dynamicContent.getParentElement();
                        return new DBTypeProcedureImpl(type, resultSet);
                    }
                };
            }
        };

        new DynamicSubcontentLoader<DBType>(TYPE, TYPE, false) {

            public boolean match(DBType type, DynamicContent dynamicContent) {
                DBType superType = type.getSuperType();
                DBType thisType = (DBType) dynamicContent.getParentElement();
                return superType != null && superType.equals(thisType);
            }

            @Override
            public DynamicContentLoader<DBType> createAlternativeLoader() {
                return null;
            }
        };
    }



    private class SpecSourceCodeLoader extends DBSourceCodeLoader {
        SpecSourceCodeLoader(DBObject object) {
            super(object, false);
        }

        public ResultSet loadSourceCode(DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectSourceCode(getSchema().getName(), getName(), "TYPE", connection);
        }
    }

    private class BodySourceCodeLoader extends DBSourceCodeLoader {
        BodySourceCodeLoader(DBObject object) {
            super(object, true);
        }

        public ResultSet loadSourceCode(DBNConnection connection) throws SQLException {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectSourceCode(getSchema().getName(), getName(), "TYPE BODY", connection);
        }
    }

    private static DBObjectTimestampLoader SPEC_TIMESTAMP_LOADER = new DBObjectTimestampLoader("TYPE") {};
    private static DBObjectTimestampLoader BODY_TIMESTAMP_LOADER = new DBObjectTimestampLoader("TYPE BODY") {};

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
        if (getParentObject() instanceof DBSchema) {
            return contentType == DBContentType.CODE_SPEC ? "type_spec" :
                   contentType == DBContentType.CODE_BODY ? "type_body" : null;
        }
        return null;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBType) {
            DBType type = (DBType) o;
            if (getParentObject().equals(type.getParentObject())) {
                if ((type.isCollection() && isCollection()) ||
                        (!type.isCollection() && !isCollection())) return super.compareTo(o); else
                return type.isCollection() ? -1 : 1;
            }
        }
        return super.compareTo(o);
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = super.createNavigationLists();

        DBType superType = getSuperType();
        if (superType != null) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<>("Super Type", superType));
        }
        if (subTypes != null && subTypes.size() > 0) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<>("Sub Types", subTypes.getObjects()));
        }
        if (isCollection()) {
            DBDataType dataType = getCollectionElementType();
            if (dataType != null && dataType.isDeclared()) {
                DBType collectionElementType = dataType.getDeclaredType();
                if (collectionElementType != null) {
                    objectNavigationLists.add(new DBObjectNavigationListImpl("Collection element type", collectionElementType));
                }
            }
        }



        return objectNavigationLists;
    }

    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return contentType == DBContentType.CODE_SPEC ? SPEC_TIMESTAMP_LOADER :
               contentType == DBContentType.CODE_BODY ? BODY_TIMESTAMP_LOADER : null;
    }

}
