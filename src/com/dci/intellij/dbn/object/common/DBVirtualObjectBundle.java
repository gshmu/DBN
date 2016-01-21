package com.dci.intellij.dbn.object.common;

import javax.swing.Icon;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.connection.VirtualConnectionHandler;
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
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;

public class DBVirtualObjectBundle implements DBObjectBundle{
    private VirtualConnectionHandler connectionHandler;

    public DBVirtualObjectBundle(VirtualConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public List<DBSchema> getSchemas() {
        return Collections.emptyList();
    }

    @Override
    public List<DBUser> getUsers() {
        return Collections.emptyList();
    }

    @Override
    public List<DBRole> getRoles() {
        return Collections.emptyList();
    }

    @Override
    public List<DBSystemPrivilege> getSystemPrivileges() {
        return Collections.emptyList();
    }

    @Override
    public List<DBCharset> getCharsets() {
        return Collections.emptyList();
    }

    @Override
    public List<DBNativeDataType> getNativeDataTypes() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public DBNativeDataType getNativeDataType(String name) {
        return null;
    }

    @Nullable
    @Override
    public DBSchema getSchema(String name) {
        return null;
    }

    @Nullable
    @Override
    public DBSchema getPublicSchema() {
        return null;
    }

    @Nullable
    @Override
    public DBSchema getUserSchema() {
        return null;
    }

    @Nullable
    @Override
    public DBUser getUser(String name) {
        return null;
    }

    @Nullable
    @Override
    public DBRole getRole(String name) {
        return null;
    }

    @Nullable
    @Override
    public DBPrivilege getPrivilege(String name) {
        return null;
    }

    @Nullable
    @Override
    public DBSystemPrivilege getSystemPrivilege(String name) {
        return null;
    }

    @Nullable
    @Override
    public DBCharset getCharset(String name) {
        return null;
    }

    @NotNull
    @Override
    public List<DBDataType> getCachedDataTypes() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public DBObject getObject(DatabaseObjectIdentifier objectIdentifier) {
        return null;
    }

    @Nullable
    @Override
    public DBObject getObject(DBObjectType objectType, String name) {
        return null;
    }

    @Nullable
    @Override
    public DBObject getObject(DBObjectType objectType, String name, int overload) {
        return null;
    }

    @Override
    public void lookupObjectsOfType(LookupConsumer consumer, DBObjectType objectType) throws ConsumerStoppedException {

    }

    @Override
    public void lookupChildObjectsOfType(LookupConsumer consumer, DBObject parentObject, DBObjectType objectType, ObjectTypeFilter filter, DBSchema currentSchema) throws ConsumerStoppedException {

    }

    @Override
    public void refreshObjectsStatus(DBSchemaObject requester) {

    }

    @Override
    public DBObjectListContainer getObjectListContainer() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @NotNull
    @Override
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    @Override
    public void initTreeElement() {

    }

    @Override
    public boolean canExpand() {
        return false;
    }

    @Override
    public int getTreeDepth() {
        return 0;
    }

    @Override
    public boolean isTreeStructureLoaded() {
        return false;
    }

    @Override
    public BrowserTreeNode getTreeChild(int index) {
        return null;
    }

    @Nullable
    @Override
    public BrowserTreeNode getTreeParent() {
        return null;
    }

    @Override
    public List<? extends BrowserTreeNode> getTreeChildren() {
        return null;
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {

    }

    @Override
    public void rebuildTreeChildren() {

    }

    @Override
    public int getTreeChildCount() {
        return 0;
    }

    @Override
    public boolean isLeafTreeElement() {
        return false;
    }

    @Override
    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return 0;
    }

    @Override
    public Icon getIcon(int flags) {
        return null;
    }

    @Override
    public String getPresentableText() {
        return null;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean unused) {
        return null;
    }

    @Override
    public String getPresentableTextDetails() {
        return null;
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return null;
    }

    @NotNull
    @Override
    public Project getProject() {
        return null;
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return null;
    }

    @Override
    public GenericDatabaseElement getUndisposedElement() {
        return null;
    }

    @Nullable
    @Override
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void dispose() {

    }

    @Nullable
    @Override
    public String getName() {
        return null;
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return null;
    }

    @Override
    public void navigate(boolean requestFocus) {

    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public String getToolTip() {
        return null;
    }
}
