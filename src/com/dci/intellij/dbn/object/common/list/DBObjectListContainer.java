package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.MultipleContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapterImpl;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.Compactable;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.dispose.Failsafe.check;

public class DBObjectListContainer extends StatefulDisposable.Base implements StatefulDisposable, Compactable {
    private GenericDatabaseElement owner;
    private List<DBObjectList<?>> objectLists;

    public DBObjectListContainer(@NotNull GenericDatabaseElement owner) {
        this.owner = owner;
    }

    @Override
    public void compact() {
        objectLists = CollectionUtil.compact(objectLists);
    }

    @Nullable
    public Collection<DBObjectList<?>> getObjectLists() {
        return objectLists;
    }

    public void visitLists(@NotNull DBObjectListVisitor visitor, boolean visitInternal) {
        if (objectLists != null) {
            Safe.run(() -> {
                checkDisposed(visitor);
                for (DBObjectList<?> objectList : objectLists) {
                    if (check(objectList) && (visitInternal || !objectList.isInternal())) {
                        checkDisposed(visitor);
                        ProgressMonitor.checkCancelled();

                        visitor.visit(objectList);
                    }
                }
            });
        }
    }

    private void checkDisposed(DBObjectListVisitor visitor) {
        Failsafe.nd(this);
        Failsafe.nd(visitor);
    }

    @NotNull
    public <T extends DBObject> List<T> getObjects(DBObjectType objectType, boolean internal) {
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
    private <T extends DBObject> DBObjectList<T> findObjectList(DBObjectType objectType) {
        return objectLists == null ? null : (DBObjectList<T>) objectLists.stream().filter(list -> list.getObjectType() == objectType).findFirst().orElse(null);
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> getObjectList(DBObjectType objectType) {
        DBObjectList<T> objectList = findObjectList(objectType);
        if (check(objectList) && !objectList.isInternal()) {
            return objectList;
        }
        return null;
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> getInternalObjectList(DBObjectType objectType) {
        DBObjectList<T> objectList = findObjectList(objectType);
        if (check(objectList) && objectList.isInternal()) {
            return objectList;
        }
        return null;
    }


    public DBObject getObject(DBObjectType objectType, String name, short overload) {
        DBObjectList<?> objectList = getObjectList(objectType);
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

    public <T extends DBObject> T getInternalObject(DBObjectType objectType, String name, short overload) {
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

    public DBObject getObject(String name, short overload) {
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
    public DBObject getObjectForParentType(DBObjectType parentObjectType, String name, short overload, boolean lookupInternal) {
        if (objectLists != null) {
            for (DBObjectList objectList : objectLists) {
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

    public DBObject getObjectNoLoad(String name, short overload) {
        if (objectLists != null) {
            for (DBObjectList objectList : objectLists) {
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
        return Failsafe.nn(owner);
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

    public void addObjectList(DBObjectList<?> objectList) {
        if (objectList != null) {
            // enum map holds an array of all enum elements!!
            if (objectLists == null) objectLists = new ArrayList<>();
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
                if (check(objectList)) {
                    objectList.refresh();
                    checkDisposed();
                }
            }
        }
    }

    public void load() {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists) {
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
        SafeDisposer.dispose(objectLists, false, false);
        owner = null;
        nullify();
    }
}
