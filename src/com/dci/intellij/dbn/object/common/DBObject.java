package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.code.common.lookup.LookupValueProvider;
import com.dci.intellij.dbn.common.Referenceable;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.vfs.DatabaseObjectFile;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface DBObject extends BrowserTreeNode, PsiNamedElement, DynamicContentElement, LookupValueProvider, Presentable, Referenceable {
    List<DBObject> EMPTY_LIST = new ArrayList<DBObject>();

    DBObjectType getObjectType();
    boolean isOfType(DBObjectType objectType);

    DBLanguageDialect getLanguageDialect(DBLanguage language);
    
    DBObjectAttribute[] getObjectAttributes();
    DBObjectAttribute getNameAttribute();

    @NotNull
    String getName();
    String getQuotedName(boolean quoteAlways);
    boolean needsNameQuoting();
    String getQualifiedName();
    String getQualifiedNameWithType();
    String getQualifiedNameWithConnectionId();
    String getNavigationTooltipText();
    String getTypeName();
    Icon getIcon();
    Icon getOriginalIcon();

    DBUser getOwner();
    DBSchema getSchema();
    DBObject getParentObject();
    DBObjectBundle getObjectBundle();

    @Nullable
    DBObject getDefaultNavigationObject();
    List<DBObject> getChildObjects(DBObjectType objectType);
    DBObject getChildObject(DBObjectType objectType, String name, boolean lookupHidden);
    DBObject getChildObject(String name, boolean lookupHidden);

    List<DBObjectNavigationList> getNavigationLists();

    @Nullable
    DBObjectListContainer getChildObjects();

    @Nullable
    DBObjectRelationListContainer getChildObjectRelations();
    String extractDDL() throws SQLException;

    @Nullable
    DBObject getUndisposedElement();

    DBObjectProperties getProperties();
    DBOperationExecutor getOperationExecutor();

    @NotNull
    DatabaseObjectFile getVirtualFile();
    List<PresentableProperty> getPresentableProperties();
    EnvironmentType getEnvironmentType();
    DBObjectRef getRef();

}
