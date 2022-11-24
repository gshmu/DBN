package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilderProvider;
import com.dci.intellij.dbn.common.Referenceable;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeProvider;
import com.dci.intellij.dbn.common.path.Node;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.Consumer;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceContext;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public interface DBObject extends
        PropertyHolder<DBObjectProperty>,
        BrowserTreeNode,
        DynamicContentElement,
        LookupItemBuilderProvider,
        Referenceable,
        EnvironmentTypeProvider,
        Presentable {

    @NotNull
    @Override
    String getName();

    @NotNull
    @Override
    ConnectionId getConnectionId();

    @NotNull
    DBObjectType getObjectType();

    boolean isOfType(DBObjectType objectType);

    DBLanguageDialect getLanguageDialect(DBLanguage language);
    
    DBObjectAttribute[] getObjectAttributes();
    DBObjectAttribute getNameAttribute();

    String getQuotedName(boolean quoteAlways);
    boolean needsNameQuoting();
    String getQualifiedNameWithType();
    String getNavigationTooltipText();
    String getTypeName();
    @Override
    @Nullable
    Icon getIcon();
    Icon getOriginalIcon();
    DBContentType getContentType();

    @Nullable
    DBUser getOwner();

    DBObject getParentObject();

    @Nullable DBObject getDefaultNavigationObject();

    @NotNull
    List<DBObject> getChildObjects(DBObjectType objectType);

    void collectChildObjects(DBObjectType objectType, Consumer<? super DBObject> consumer);

    @Nullable
    DBObjectList<? extends DBObject> getChildObjectList(DBObjectType objectType);

    DBObject getChildObject(DBObjectType objectType, String name, boolean lookupHidden);

    DBObject getChildObject(DBObjectType objectType, String name, short overload, boolean lookupHidden);

    @Nullable
    DBObject getChildObject(String name, boolean lookupHidden);

    @Nullable
    DBObject getChildObject(String name, short overload, boolean lookupHidden);

    @Nullable
    default DBObject getChildObject(String name, short overload, boolean lookupHidden, Node<DBObject> lookupPath) {
        return null;
    }

    List<String> getChildObjectNames(DBObjectType objectType);

    List<DBObjectNavigationList> getNavigationLists();

    void initChildren();

    @Nullable
    DBObjectListContainer getChildObjects();

    @Nullable
    String extractDDL() throws SQLException;

    DBOperationExecutor getOperationExecutor();

    @NotNull
    DBObjectVirtualFile getVirtualFile();
    List<PresentableProperty> getPresentableProperties();
    @Override
    DBObjectRef ref();

    boolean isValid();

    boolean isVirtual();

    boolean isParentOf(DBObject object);

    @NotNull
    DBObjectPsiCache getPsiCache();

    void refresh(DBObjectType objectType);

    @Override
    default DynamicContentType<?> getDynamicContentType() {
        return getObjectType();
    }

    default DatabaseInterfaceContext createInterfaceContext() {
        return DatabaseInterfaceContext.create(getConnection(), getSchemaId(), true);
    }
}
