package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.util.Consumer;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.data.type.DBDataTypeBundle;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.DBCharset;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DBObjectBundle extends BrowserTreeNode, StatefulDisposable {
    List<DBConsole> getConsoles();

    List<DBSchema> getSchemas();

    List<DBSchema> getPublicSchemas();

    List<SchemaId> getSchemaIds();

    @Nullable
    List<DBUser> getUsers();

    @Nullable
    List<DBRole> getRoles();

    @Nullable
    List<DBSystemPrivilege> getSystemPrivileges();

    @Nullable
    List<DBCharset> getCharsets();

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
    DBDataTypeBundle getDataTypes();

    @Nullable
    DBObject getObject(DatabaseObjectIdentifier objectIdentifier);

    @Nullable
    DBObject getObject(DBObjectType objectType, String name);

    @Nullable
    DBObject getObject(DBObjectType objectType, String name, short overload);

    void lookupObjectsOfType(Consumer<? super DBObject> consumer, DBObjectType objectType);

    void lookupChildObjectsOfType(Consumer<? super DBObject> consumer, DBObject parentObject, DBObjectType objectType, ObjectTypeFilter filter, DBSchema currentSchema);

    void refreshObjectsStatus(DBSchemaObject requester);

    DBObjectListContainer getObjectLists();

    <T extends DBObject> DBObjectList<T> getObjectList(DBObjectType objectType);

    LookupItemBuilder getLookupItemBuilder(DBObjectRef<?> objectRef, DBLanguage<?> language);

    PsiDirectory getObjectListPsiDirectory(DBObjectList objectList);

    DBObjectPsiCache getObjectPsiCache(DBObjectRef<?> objectRef);

    DBObjectVirtualFile<?> getObjectVirtualFile(DBObjectRef<?> objectRef);

    PsiFile getFakeObjectFile();

    boolean isValid();
}
