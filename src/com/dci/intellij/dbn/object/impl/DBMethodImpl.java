package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBArgumentMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBMethodMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.COMPILABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.DEBUGABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.DETERMINISTIC;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.INVALIDABLE;
import static com.dci.intellij.dbn.object.type.DBObjectType.ARGUMENT;
import static com.dci.intellij.dbn.object.type.DBObjectType.METHOD;

public abstract class DBMethodImpl<M extends DBMethodMetadata> extends DBSchemaObjectImpl<M> implements DBMethod {
    protected DBObjectList<DBArgument> arguments;
    protected int position;
    protected int overload;
    private DBLanguage language;

    DBMethodImpl(DBSchemaObject parent, M resultSet) throws SQLException {
        super(parent, resultSet);
    }

    DBMethodImpl(DBSchema schema, M resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected String initObject(M metadata) throws SQLException {
        set(DETERMINISTIC, metadata.isDeterministic());
        overload = metadata.getOverload();
        position = metadata.getPosition();
        language = DBLanguage.getLanguage(metadata.getLanguage());
        return null;
    }

    @Override
    public void initProperties() {
        super.initProperties();
        properties.set(COMPILABLE, true);
        properties.set(INVALIDABLE, true);
        properties.set(DEBUGABLE, true);
    }

    @Override
    public void initStatus(M metadata) throws SQLException {
        boolean isValid = metadata.isValid();
        boolean isDebug = metadata.isDebug();
        DBObjectStatusHolder objectStatus = getStatus();
        objectStatus.set(DBObjectStatus.VALID, isValid);
        objectStatus.set(DBObjectStatus.DEBUG, isDebug);
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBObjectListContainer container = initChildObjects();
        arguments = container.createSubcontentObjectList(ARGUMENT, this, getSchema());
    }

    @Override
    @NotNull
    public DBLanguage getLanguage() {
        return language;
    }

    @Override
    public boolean isEditable(DBContentType contentType) {
        return getContentType() == DBContentType.CODE && contentType == DBContentType.CODE;
    }

    @Override
    public boolean isDeterministic() {
        return is(DETERMINISTIC);
    }

    @Override
    public boolean hasDeclaredArguments() {
        for (DBArgument argument : getArguments()) {
            if (argument.getDataType().isDeclared()) {
                return true;
            }
        }
        return false; 
    }

    @Override
    public List<DBArgument> getArguments() {
        return arguments.getObjects();
    }

    @Override
    public DBArgument getReturnArgument() {
        return null;
    }

    @Override
    public DBArgument getArgument(String name) {
        return (DBArgument) getObjectByName(getArguments(), name);
    }

    @Override
    public int getOverload() {
        return overload;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getPresentableTextDetails() {
        return overload > 0 ? " #" + overload : "";
    }

    @Override
    public boolean isProgramMethod() {
        return false;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        int result = super.compareTo(o);
        if (result == 0) {
            DBMethod method = (DBMethod) o;
            return overload - method.getOverload();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (super.equals(obj)) {
            DBMethod method = (DBMethod) obj;
            return method.getOverload() == overload;
        }
        return false;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(arguments);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        new DynamicSubcontentLoader<DBArgument, DBArgumentMetadata>(METHOD, ARGUMENT, true) {
            @Override
            public boolean match(DBArgument argument, DynamicContent dynamicContent) {
                DBMethod method = (DBMethod) dynamicContent.getParentElement();
                DBMethod argumentMethod = argument.getMethod();
                return argumentMethod != null && argumentMethod.equals(method) && argument.getOverload() == method.getOverload();
            }

            @Override
            public DynamicContentLoader<DBArgument, DBArgumentMetadata> createAlternativeLoader() {
                return new DynamicContentResultSetLoader<DBArgument, DBArgumentMetadata>(METHOD, ARGUMENT, false, true) {
                    @Override
                    public ResultSet createResultSet(DynamicContent<DBArgument> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadataInterface = dynamicContent.getMetadataInterface();
                        DBMethod method = (DBMethod) dynamicContent.getParentElement();
                        String ownerName = method.getSchema().getName();
                        int overload = method.getOverload();
                        DBProgram program = method.getProgram();
                        if (program == null) {
                            return metadataInterface.loadMethodArguments(
                                    ownerName,
                                    method.getName(),
                                    method.getMethodType().id(),
                                    overload,
                                    connection);
                        } else {
                            return metadataInterface.loadProgramMethodArguments(
                                    ownerName,
                                    program.getName(),
                                    method.getName(),
                                    overload,
                                    connection);
                        }
                    }

                    @Override
                    public DBArgument createElement(DynamicContent<DBArgument> content, DBArgumentMetadata metadata, LoaderCache cache) throws SQLException {
                        DBMethod method = (DBMethod) content.getParentElement();
                        return new DBArgumentImpl(method, metadata);
                    }
                };
            }
        };
    }
}
