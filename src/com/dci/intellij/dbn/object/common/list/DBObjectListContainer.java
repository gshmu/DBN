package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.MultipleContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapterImpl;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.Compactable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.dci.intellij.dbn.common.dispose.Failsafe.check;

public class DBObjectListContainer extends DisposableBase implements Disposable, Compactable {
    private Map<DBObjectType, DBObjectList<DBObject>> objectLists;
    private WeakRef<GenericDatabaseElement> owner;

    public DBObjectListContainer(@NotNull GenericDatabaseElement owner) {
        this.owner = WeakRef.from(owner);
    }

    @Override
    public void compact() {
        CollectionUtil.compact(objectLists);
    }

    @Nullable
    public Collection<DBObjectList<DBObject>> getObjectLists() {
        return objectLists == null ? null : objectLists.values();
    }

    public void visitLists(DBObjectListVisitor visitor, boolean visitInternal) {
        Failsafe.guarded(() -> {
            if (objectLists != null) {
                checkDisposed(visitor);
                for (DBObjectList<DBObject> objectList : objectLists.values()) {
                    if (check(objectList) && (visitInternal || !objectList.isInternal())) {
                        checkDisposed(visitor);
                        ProgressMonitor.checkCancelled();

                        visitor.visitObjectList(objectList);
                    }
                }
            }
        });
    }

    private void checkDisposed(DBObjectListVisitor visitor) {
        Failsafe.nd(this);
        Failsafe.nd(visitor);
    }

    @NotNull
    public List<? extends DBObject> getObjects(DBObjectType objectType, boolean internal) {
        DBObjectList objectList = internal ?
                getInternalObjectList(objectType) :
                getObjectList(objectType);
        if (objectList == null) {
            return java.util.Collections.emptyList();
        } else {
            return objectList.getObjects();
        }
    }

    @Nullable
    public DBObjectList getObjectList(DBObjectType objectType) {
        if (objectLists != null) {
            DBObjectList<DBObject> objectList = objectLists.get(objectType);
            if (check(objectList) && !objectList.isInternal()) {
                return objectList;
            }
        }
        return null;
    }

    @Nullable
    public DBObjectList getInternalObjectList(DBObjectType objectType) {
        if (objectLists != null) {
            DBObjectList<DBObject> objectList = objectLists.get(objectType);
            if (check(objectList) && objectList.isInternal()) {
                return objectList;
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
            for (DBObjectList objectList : objectLists.values()) {
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
            for (DBObjectList objectList : objectLists.values()) {
                if ((!objectList.isInternal() || lookupInternal) && check(objectList)) {
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
        GenericDatabaseElement owner = getOwner();
        ConnectionHandler connectionHandler = owner.getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connectionHandler);
        return compatibilityInterface.supportsObjectType(objectType.getTypeId());
    }

    public DBObject getObjectNoLoad(String name, int overload) {
        if (objectLists != null) {
            for (DBObjectList objectList : objectLists.values()) {
                if (check(objectList) && objectList.isLoaded() && !objectList.isDirty()) {
                    DBObject object = objectList.getObject(name, overload);
                    if (object != null) {
                        GenericDatabaseElement owner = getOwner();
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

    @NotNull
    private GenericDatabaseElement getOwner() {
        return owner.nn();
    }


    /*************************************************************
     *             DBObjectList factory utilities                *
     *************************************************************/
    @Nullable
    public <T extends DBObject> DBObjectList<T>  createObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            DynamicContentStatus... statuses) {
        if (isSupported(objectType)) {
            return createObjectList(objectType, treeParent, BasicDependencyAdapter.INSTANCE, statuses);
        }
        return null;
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T>  createObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            DBObjectList[] sourceContents,
            DynamicContentStatus... statuses) {
        if (isSupported(objectType)) {
            ContentDependencyAdapter dependencyAdapter = new MultipleContentDependencyAdapter(sourceContents);
            return createObjectList(objectType, treeParent, dependencyAdapter, statuses);
        }
        return null;
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            GenericDatabaseElement sourceContentHolder,
            DynamicContentType sourceContentType,
            DynamicContentStatus... statuses) {
        if (isSupported(objectType)) {
            if (sourceContentHolder != null && sourceContentHolder.getDynamicContent(sourceContentType) != null) {
                ContentDependencyAdapter dependencyAdapter =
                        new SubcontentDependencyAdapterImpl(
                                sourceContentHolder,
                                sourceContentType
                        );
                return createObjectList(objectType, treeParent, dependencyAdapter, statuses);
            }
        }
        return null;
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            DBObject sourceContentHolder,
            DynamicContentStatus... statuses) {
        if (isSupported(objectType)) {
            if (sourceContentHolder.getDynamicContent(objectType) != null) {
                ContentDependencyAdapter dependencyAdapter =
                        new SubcontentDependencyAdapterImpl(
                                sourceContentHolder,
                                objectType
                        );
                return createObjectList(objectType, treeParent, dependencyAdapter, statuses);
            }
        }
        return null;
    }

    private <T extends DBObject> DBObjectList<T> createObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentStatus... statuses) {
        DBObjectList<T> objectList = new DBObjectListImpl<T>(objectType, treeParent, dependencyAdapter, statuses);
        addObjectList(objectList);

        return objectList;
    }

    public void addObjectList(DBObjectList objectList) {
        if (objectList != null) {
            if (objectLists == null) objectLists = new THashMap<>();
            objectLists.put(objectList.getObjectType(), objectList);
        }
    }

    public void reload() {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists.values()) {
                objectList.reload();
                checkDisposed();
            }
        }
    }

    public void refresh() {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists.values()) {
                if (check(objectList)) {
                    objectList.refresh();
                    checkDisposed();
                }
            }
        }
    }

    public void load() {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists.values()) {
                if (!objectList.isInternal()) {
                    objectList.ensure();
                }
                checkDisposed();
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

    @Override
    public void disposeInner() {
        Disposer.dispose(objectLists);
        super.disposeInner();
    }
}
