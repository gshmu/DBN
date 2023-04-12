package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.common.metadata.def.DBArgumentMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBMethodMetadata;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.filter.type.ObjectTypeFilterSettings;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;
import static com.dci.intellij.dbn.object.type.DBObjectType.ARGUMENT;
import static com.dci.intellij.dbn.object.type.DBObjectType.METHOD;

@Getter
public abstract class DBMethodImpl<M extends DBMethodMetadata> extends DBSchemaObjectImpl<M> implements DBMethod {
    protected short position;
    protected short overload;
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
        DBObjectListContainer childObjects = ensureChildObjects();
        childObjects.createSubcontentObjectList(ARGUMENT, this, getSchema());
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
        return getChildObjects(ARGUMENT);
    }

    @Override
    public DBArgument getReturnArgument() {
        return null;
    }

    @Override
    public DBArgument getArgument(String name) {
        return getChildObject(ARGUMENT, name);
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
    public List<BrowserTreeNode> buildPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(getChildObjectList(ARGUMENT));
    }

    @Override
    public boolean hasVisibleTreeChildren() {
        ObjectTypeFilterSettings settings = getObjectTypeFilterSettings();
        return settings.isVisible(ARGUMENT);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/

    static {
        DynamicSubcontentLoader.create(METHOD, ARGUMENT,
                new DynamicContentResultSetLoader<DBArgument, DBArgumentMetadata>(METHOD, ARGUMENT, false, true) {
                    @Override
                    public ResultSet createResultSet(DynamicContent<DBArgument> dynamicContent, DBNConnection connection) throws SQLException {
                        DatabaseMetadataInterface metadata = dynamicContent.getMetadataInterface();
                        DBMethod method = dynamicContent.ensureParentEntity();
                        String ownerName = getSchemaName(method);
                        short overload = method.getOverload();
                        DBProgram program = method.getProgram();
                        if (program == null) {
                            return metadata.loadMethodArguments(
                                    ownerName,
                                    method.getName(),
                                    method.getMethodType().id(),
                                    overload,
                                    connection);
                        } else {
                            return metadata.loadProgramMethodArguments(
                                    ownerName,
                                    program.getName(),
                                    method.getName(),
                                    overload,
                                    connection);
                        }
                    }

                    @Override
                    public DBArgument createElement(DynamicContent<DBArgument> content, DBArgumentMetadata metadata, LoaderCache cache) throws SQLException {
                        DBMethod method = content.getParentEntity();
                        return method == null ? null : new DBArgumentImpl(method, metadata);
                    }
                });
    }
}
