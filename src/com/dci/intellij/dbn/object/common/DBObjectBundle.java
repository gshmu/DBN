package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.object.DBCharset;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DBObjectBundle extends BrowserTreeNode, Disposable {
    List<DBSchema> getSchemas();
    List<DBUser> getUsers();
    List<DBRole> getRoles();
    List<DBSystemPrivilege> getSystemPrivileges();
    List<DBCharset> getCharsets();
    List<DBNativeDataType> getNativeDataTypes();

    @Nullable
    DBNativeDataType getNativeDataType(String name);

    @Nullable
    DBSchema getSchema(String name);

    @Nullable
    DBSchema getPublicSchema();

    @Nullable
    DBSchema getUserSchema();

    @Nullable
    DBUser getUser(String name);

    @Nullable
    DBRole getRole(String name);

    @Nullable
    DBPrivilege getPrivilege(String name);

    @Nullable
    DBSystemPrivilege getSystemPrivilege(String name);

    @Nullable
    DBCharset getCharset(String name);

    @NotNull
    List<DBDataType> getCachedDataTypes();

    @Nullable
    DBObject getObject(DatabaseObjectIdentifier objectIdentifier);

    @Nullable
    DBObject getObject(DBObjectType objectType, String name);

    @Nullable
    DBObject getObject(DBObjectType objectType, String name, int overload);

    void lookupObjectsOfType(LookupConsumer consumer, DBObjectType objectType) throws ConsumerStoppedException;
    void lookupChildObjectsOfType(LookupConsumer consumer, DBObject parentObject, DBObjectType objectType, ObjectTypeFilter filter, DBSchema currentSchema) throws ConsumerStoppedException;
    void refreshObjectsStatus(DBSchemaObject requester);

    DBObjectListContainer getObjectListContainer();
    boolean isValid();

    @NotNull
    @Override
    ConnectionHandler getConnectionHandler();
}
