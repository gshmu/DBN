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
import com.dci.intellij.dbn.database.common.metadata.def.DBDataTypeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBFunctionMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBProcedureMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeAttributeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeMetadata;
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

import static com.dci.intellij.dbn.object.common.DBObjectType.*;

public class DBTypeImpl
        extends DBProgramImpl<DBTypeMetadata, DBTypeProcedure, DBTypeFunction>
        implements DBType<DBTypeProcedure, DBTypeFunction> {

    private static final List<DBTypeAttribute> EMPTY_ATTRIBUTE_LIST = Collections.unmodifiableList(new ArrayList<>(0));

    protected DBObjectList<DBTypeAttribute> attributes;
    protected DBObjectList<DBType> subTypes;

    private String superTypeOwner;
    private String superTypeName;
    private DBObjectRef<DBType> superType;

    private DBDataType.Ref collectionElementTypeRef;
    private DBDataType collectionElementType;

    private DBNativeDataType nativeDataType;

    DBTypeImpl(DBSchemaObject parent, DBTypeMetadata metadata) throws SQLException {
        // type functions are not editable independently
        super(parent, metadata);
        assert this.getClass() != DBTypeImpl.class;
    }

    DBTypeImpl(DBSchema schema, DBTypeMetadata metadata) throws SQLException {
        super(schema, metadata);
    }

    @Override
    protected String initObject(DBTypeMetadata metadata) throws SQLException {
        String name = metadata.getTypeName();
        superTypeOwner = metadata.getSupertypeOwner();
        superTypeName = metadata.getSupertypeName();

        String typeCode = metadata.getTypeCode();
        boolean collection = metadata.isCollection();
        set(DBObjectProperty.COLLECTION, collection);

        ConnectionHandler connectionHandler = getConnectionHandler();
        nativeDataType = connectionHandler.getObjectBundle().getNativeDataType(typeCode);
        if (collection) {
            DBDataTypeMetadata collectionMetadata = metadata.getDataType().collection();
            collectionElementTypeRef = new DBDataType.Ref(collectionMetadata);
        }
        return name;
    }


    @Override
    protected void initLists() {
        super.initLists();
        if (!isCollection()) {
            DBObjectListContainer container = initChildObjects();
            DBSchema schema = getSchema();
            attributes = container.createSubcontentObjectList(TYPE_ATTRIBUTE, this, schema);
            procedures = container.createSubcontentObjectList(TYPE_PROCEDURE, this, schema);
            functions = container.createSubcontentObjectList(TYPE_FUNCTION, this, schema);
            subTypes = container.createSubcontentObjectList(TYPE, this, schema);
        }
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return TYPE;
    }

    @Override
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

    @Override
    public Icon getOriginalIcon() {
        return isCollection() ? Icons.DBO_TYPE_COLLECTION : Icons.DBO_TYPE;
    }

    @Override
    public List<DBTypeAttribute> getAttributes() {
        return attributes == null ? EMPTY_ATTRIBUTE_LIST : attributes.getObjects();
    }

    @Override
    public DBType getSuperType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (superType == null && superTypeOwner != null && superTypeName != null) {
            DBSchema schema = connectionHandler.getObjectBundle().getSchema(superTypeOwner);
            DBType type = schema == null ? null : schema.getType(superTypeName);
            superType = DBObjectRef.from(type);
            superTypeOwner = null;
            superTypeName = null;
        }
        return DBObjectRef.get(superType);
    }

    @Override
    public DBDataType getCollectionElementType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (collectionElementType == null && collectionElementTypeRef != null) {
            collectionElementType = collectionElementTypeRef.get(connectionHandler);
            collectionElementTypeRef = null;
        }
        return collectionElementType;
    }

    @Override
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

    @Override
    public List<DBType> getSubTypes() {
        return subTypes.getObjects();
    }

    @Override
    public DBNativeDataType getNativeDataType() {
        return nativeDataType;
    }

    @Override
    public boolean isCollection() {
        return is(DBObjectProperty.COLLECTION);
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
        return isCollection() ?
                EMPTY_TREE_NODE_LIST :
                DatabaseBrowserUtils.createList(attributes, procedures, functions);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        new DynamicSubcontentLoader<DBTypeAttribute, DBTypeAttributeMetadata>(TYPE, TYPE_ATTRIBUTE, true) {

            @Override
            public boolean match(DBTypeAttribute typeAttribute, DynamicContent dynamicContent) {
                DBType type = (DBType) dynamicContent.getParentElement();
                return typeAttribute.getType().equals(type);
            }

            @Override
            public DynamicContentLoader<DBTypeAttribute, DBTypeAttributeMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBTypeAttribute, DBTypeAttributeMetadata>(TYPE, TYPE_ATTRIBUTE, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBTypeAttribute> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBType type = (DBType) dynamicContent.getParentElement();
                        return metadataInterface.loadTypeAttributes(type.getSchema().getName(), type.getName(), connection);
                    }

                    @Override
                    public DBTypeAttribute createElement(DynamicContent<DBTypeAttribute> content, DBTypeAttributeMetadata metadata, LoaderCache cache) throws SQLException {
                        DBTypeImpl type = (DBTypeImpl) content.getParentElement();
                        return new DBTypeAttributeImpl(type, metadata);
                    }
                };

            }
        };

        new DynamicSubcontentLoader<DBTypeFunction, DBFunctionMetadata>(TYPE, TYPE_FUNCTION, true) {

            @Override
            public boolean match(DBTypeFunction function, DynamicContent dynamicContent) {
                DBType type = (DBType) dynamicContent.getParentElement();
                return function.getType() == type;
            }

            @Override
            public DynamicContentLoader<DBTypeFunction, DBFunctionMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBTypeFunction, DBFunctionMetadata>(TYPE, TYPE_FUNCTION, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBTypeFunction> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBType type = (DBType) dynamicContent.getParentElement();
                        return metadataInterface.loadTypeFunctions(type.getSchema().getName(), type.getName(), connection);
                    }

                    @Override
                    public DBTypeFunction createElement(DynamicContent<DBTypeFunction> content, DBFunctionMetadata metadata, LoaderCache cache) throws SQLException {
                        DBType type = (DBType) content.getParentElement();
                        return new DBTypeFunctionImpl(type, metadata);
                    }
                };
            }
        };

        new DynamicSubcontentLoader<DBTypeProcedure, DBProcedureMetadata>(TYPE, TYPE_PROCEDURE, true) {

            @Override
            public boolean match(DBTypeProcedure procedure, DynamicContent dynamicContent) {
                DBType type = (DBType) dynamicContent.getParentElement();
                return procedure.getType() == type;
            }

            @Override
            public DynamicContentLoader<DBTypeProcedure, DBProcedureMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBTypeProcedure, DBProcedureMetadata>(TYPE, TYPE_PROCEDURE, false, true) {

                    @Override
                    public ResultSet createResultSet(DynamicContent<DBTypeProcedure> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBType type = (DBType) dynamicContent.getParentElement();
                        return metadataInterface.loadTypeProcedures(type.getSchema().getName(), type.getName(), connection);
                    }

                    @Override
                    public DBTypeProcedure createElement(DynamicContent<DBTypeProcedure> content, DBProcedureMetadata metadata, LoaderCache cache) throws SQLException {
                        DBType type = (DBType) content.getParentElement();
                        return new DBTypeProcedureImpl(type, metadata);
                    }
                };
            }
        };

        new DynamicSubcontentLoader<DBType, DBTypeMetadata>(TYPE, TYPE, false) {

            @Override
            public boolean match(DBType type, DynamicContent dynamicContent) {
                DBType superType = type.getSuperType();
                DBType thisType = (DBType) dynamicContent.getParentElement();
                return superType != null && superType.equals(thisType);
            }

            @Override
            public DynamicContentLoader<DBType, DBTypeMetadata> createAlternativeLoader() {
                return null;
            }
        };
    }



    private class SpecSourceCodeLoader extends DBSourceCodeLoader {
        SpecSourceCodeLoader(DBObject object) {
            super(object, false);
        }

        @Override
        public ResultSet loadSourceCode(DBNConnection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectSourceCode(getSchema().getName(), getName(), "TYPE", connection);
        }
    }

    private class BodySourceCodeLoader extends DBSourceCodeLoader {
        BodySourceCodeLoader(DBObject object) {
            super(object, true);
        }

        @Override
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
    @Override
    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
       DBSourceCodeLoader loader =
               contentType == DBContentType.CODE_SPEC ? new SpecSourceCodeLoader(this) :
               contentType == DBContentType.CODE_BODY ? new BodySourceCodeLoader(this) : null;

       return loader == null ? null : loader.load();
    }

    @Override
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

    @Override
    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return contentType == DBContentType.CODE_SPEC ? SPEC_TIMESTAMP_LOADER :
               contentType == DBContentType.CODE_BODY ? BODY_TIMESTAMP_LOADER : null;
    }

}
