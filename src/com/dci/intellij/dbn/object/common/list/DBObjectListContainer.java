package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.MultipleContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapterImpl;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.Compactable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.dispose.FailsafeUtil.softCheck;

public class DBObjectListContainer extends DisposableBase implements Disposable, Compactable {
    private List<DBObjectList<DBObject>>  objectLists;
    private GenericDatabaseElement owner;

    public DBObjectListContainer(@NotNull GenericDatabaseElement owner) {
        this.owner = owner;
    }

    public void compact() {
        CollectionUtil.compactElements(objectLists);
    }

    @Nullable
    public List<DBObjectList<DBObject>> getObjectLists() {
        return objectLists;
    }

    public void visitLists(DBObjectListVisitor visitor, boolean visitInternal) {
        try {
            if (objectLists != null) {
                checkDisposed(visitor);
                for (DBObjectList<DBObject> objectList : objectLists) {
                    if (softCheck(objectList) && (visitInternal || !objectList.isInternal())) {
                        checkDisposed(visitor);
                        visitor.visitObjectList(objectList);
                    }
                }
            }
        } catch (ProcessCanceledException ignore) {}
    }

    private void checkDisposed(DBObjectListVisitor visitor) {
        FailsafeUtil.check(this);
        FailsafeUtil.check(visitor);
    }

    @NotNull
    public List<? extends DBObject> getObjects(DBObjectType objectType, boolean internal) {
        DBObjectList objectList = internal ?
                getInternalObjectList(objectType) :
                getObjectList(objectType);
        if (objectList == null) {
            return Collections.emptyList();
        } else {
            return objectList.getObjects();
        }
    }

    @Nullable
    public DBObjectList getObjectList(DBObjectType objectType) {
        if (objectLists != null && !objectLists.isEmpty()) {
            for (DBObjectList<DBObject> objectList : objectLists) {
                if (softCheck(objectList) && objectList.getObjectType() == objectType) {
                    return objectList;
                }
            }

        }
        return null;
    }

    @Nullable
    public DBObjectList getInternalObjectList(DBObjectType objectType) {
        if (objectLists != null) {
            for (DBObjectList<DBObject> objectList : objectLists) {
                if (softCheck(objectList) && objectList.getObjectType() == objectType && objectList.isInternal()) {
                    return objectList;
                }
            }
        }
        return null;
    }


    public DBObject getObject(DBObjectType objectType, String name, int overload) {
        DBObjectList objectList = getObjectList(objectType);
        if (objectList == null) {
            objectList = getInternalObjectList(objectType);
        }
        if (objectList != null) {
            return objectList.getObject(name, overload);
        }

        if (objectType.getInheritingTypes().size() > 0) {
            Set<DBObjectType> objectTypes = objectType.getInheritingTypes();
            for (DBObjectType objType : objectTypes) {
                DBObject object = getObject(objType, name, overload);
                if (object != null) {
                    return object;
                }
            }
        }

        return null;
    }

    public <T extends DBObject> T getInternalObject(DBObjectType objectType, String name, int overload) {
        if (objectType.isGeneric()) {
            Set<DBObjectType> objectTypes = objectType.getInheritingTypes();
            for (DBObjectType objType : objectTypes) {
                DBObjectList<T> objectList = getInternalObjectList(objType);
                if (objectList != null) {
                    T object = objectList.getObject(name, overload);
                    if (object != null) {
                        return object;
                    }
                }
            }
        } else {
            DBObjectList<T> objectList = getInternalObjectList(objectType);
            if (objectList != null) {
                return objectList.getObject(name, overload);
            }
        }
        return null;
    }

    public DBObject getObject(String name, int overload) {
        if (objectLists != null) {
            for (DBObjectList objectList : objectLists) {
                DBObject object = objectList.getObject(name, overload);
                if (object != null) {
                    return object;
                }
            }
        }
        return null;
    }

    @Nullable
    public DBObject getObjectForParentType(DBObjectType parentObjectType, String name, int overload, boolean lookupInternal) {
        if (objectLists != null) {
            for (DBObjectList objectList : objectLists) {
                if (softCheck(objectList) && (!objectList.isInternal() || lookupInternal)) {
                    DBObjectType objectType = objectList.getObjectType();
                    if (objectType.getParents().contains(parentObjectType)) {
                        DBObject object = objectList.getObject(name, overload);
                        if (object != null) {
                            return object;
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean isSupported(DBObjectType objectType) {
        ConnectionHandler connectionHandler = owner.getConnectionHandler();
        return connectionHandler == null ||
                DatabaseCompatibilityInterface.getInstance(connectionHandler).supportsObjectType(objectType.getTypeId());
    }

    public DBObject getObjectNoLoad(String name, int overload) {
        if (objectLists != null) {
            for (DBObjectList objectList : objectLists) {
                if (softCheck(objectList) && objectList.isLoaded() && !objectList.isDirty()) {
                    DBObject object = objectList.getObject(name, overload);
                    if (object != null) {
                        if (owner instanceof DBObject) {
                            DBObject ownerObject = (DBObject) owner;
                            if (ownerObject.isParentOf(object)) {
                                return object;
                            }
                        }
                    }
                }
            }
        }
        return null;

    }

     public <T extends DBObject> DBObjectList<T>  createObjectList(
             @NotNull DBObjectType objectType,
             @NotNull BrowserTreeNode treeParent,
             DynamicContentLoader loader,
             DynamicContentStatus ... statuses) {
        if (isSupported(objectType)) {
            return createObjectList(objectType, treeParent, loader, BasicDependencyAdapter.INSTANCE, statuses);
        }
        return null;
    }

    public <T extends DBObject> DBObjectList<T>  createObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            DynamicContentLoader loader,
            DBObjectList[] sourceContents,
            DynamicContentStatus ... statuses) {
        if (isSupported(objectType)) {
            ContentDependencyAdapter dependencyAdapter = new MultipleContentDependencyAdapter(sourceContents);
            return createObjectList(objectType, treeParent, loader, dependencyAdapter, statuses);
        }
        return null;
    }

    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            DynamicContentLoader loader,
            GenericDatabaseElement sourceContentHolder,
            DynamicContentType sourceContentType,
            DynamicContentStatus ... statuses) {
        if (isSupported(objectType)) {
            if (sourceContentHolder != null && sourceContentHolder.getDynamicContent(sourceContentType) != null) {
                ContentDependencyAdapter dependencyAdapter =
                        new SubcontentDependencyAdapterImpl(
                                sourceContentHolder,
                                sourceContentType
                        );
                return createObjectList(objectType, treeParent, loader, dependencyAdapter, statuses);
            }
        }
        return null;
    }

    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            DynamicContentLoader loader,
            DBObject sourceContentHolder,
            DynamicContentStatus ... statuses) {
        if (isSupported(objectType)) {
            if (sourceContentHolder.getDynamicContent(objectType) != null) {
                ContentDependencyAdapter dependencyAdapter =
                        new SubcontentDependencyAdapterImpl(
                                sourceContentHolder,
                                objectType
                        );
                return createObjectList(objectType, treeParent, loader, dependencyAdapter, statuses);
            }
        }
        return null;
    }

    private <T extends DBObject> DBObjectList<T> createObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            DynamicContentLoader<T> loader,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentStatus ... statuses) {
        DBObjectList<T> objectList = new DBObjectListImpl<T>(objectType, treeParent, loader, dependencyAdapter, statuses);
        addObjectList(objectList);

        return objectList;
    }

    public void addObjectList(DBObjectList objectList) {
        if (objectList != null) {
            if (objectLists == null) objectLists = new ArrayList<DBObjectList<DBObject>>();
            objectLists.add(objectList);
        }
    }

    public void reload() {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists) {
                objectList.reload();
                checkDisposed();
            }
        }
    }

    public void refresh() {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists) {
                if (softCheck(objectList)) {
                    objectList.refresh();
                    checkDisposed();
                }
            }
        }
    }

    public void load() {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists) {
                if (softCheck(objectList)) {
                    if (objectList.isInternal()) {
                        DBObjectType objectType = objectList.getObjectType();
                        if (!objectType.isOneOf(
                                DBObjectType.ANY,
                                DBObjectType.OUTGOING_DEPENDENCY,
                                DBObjectType.INCOMING_DEPENDENCY)) {
                            objectList.load(false);
                        }
                    } else {
                        objectList.load(false);
                    }
                    checkDisposed();
                }
                }
        }
    }

    public void loadObjectList(DBObjectType objectType) {
        DBObjectList objectList = getObjectList(objectType);
        if (objectList == null) objectList = getInternalObjectList(objectType);
        if (objectList != null) {
            objectList.getElements();
        }
    }

    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            owner = null;
            DisposerUtil.dispose(objectLists);
        }
    }
}
