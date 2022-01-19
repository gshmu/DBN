package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.DualContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapterImpl;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.dispose.Failsafe.check;

@Getter
public final class DBObjectListContainer implements StatefulDisposable {
    private static final DBObjectList<?>[] DISPOSED_OBJECTS = new DBObjectList[0];
    private static final DBObjectRelationList[] DISPOSED_OBJECT_RELATIONS = new DBObjectRelationList[0];

    private GenericDatabaseElement owner;
    private DBObjectList<?>[] objects;
    private DBObjectRelationList[] objectRelations;

    public DBObjectListContainer(@NotNull GenericDatabaseElement owner) {
        this.owner = owner;
    }

    @NotNull
    private GenericDatabaseElement getOwner() {
        return Failsafe.nn(owner);
    }

    public void visitObjects(@NotNull DBObjectListVisitor visitor, boolean visitInternal) {
        if (objects != null) {
            Safe.run(() -> {
                checkDisposed(visitor);
                for (DBObjectList<?> objectList : objects) {
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
        DBObjectList<?> objects = internal ?
                getInternalObjects(objectType) :
                getObjects(objectType);
        if (objects == null) {
            return java.util.Collections.emptyList();
        } else {
            return (List<T>) objects.getObjects();
        }
    }

    @Nullable
    private <T extends DBObject> DBObjectList<T> findObjects(DBObjectType objectType) {
        if (objects != null) {
            for (DBObjectList<?> objectList : objects) {
                if (objectList.getObjectType() == objectType) {
                    return (DBObjectList<T>) objectList;
                }
            }
        }
        return null;
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> getObjects(DBObjectType objectType) {
        DBObjectList<T> objectList = findObjects(objectType);
        if (check(objectList) && !objectList.isInternal()) {
            return objectList;
        }
        return null;
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> getInternalObjects(DBObjectType objectType) {
        DBObjectList<T> objectList = findObjects(objectType);
        if (check(objectList) && objectList.isInternal()) {
            return objectList;
        }
        return null;
    }


    public DBObject getObject(DBObjectType objectType, String name, short overload) {
        DBObjectList<?> objectList = getObjects(objectType);
        if (objectList == null) {
            objectList = getInternalObjects(objectType);
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
                DBObjectList<T> objectList = getInternalObjects(objType);
                if (objectList != null) {
                    T object = objectList.getObject(name, overload);
                    if (object != null) {
                        return object;
                    }
                }
            }
        } else {
            DBObjectList<T> objectList = getInternalObjects(objectType);
            if (objectList != null) {
                return objectList.getObject(name, overload);
            }
        }
        return null;
    }

    public DBObject getObject(String name, short overload) {
        if (objects != null) {
            for (DBObjectList<?> objectList : objects) {
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
        if (objects != null) {
            for (DBObjectList<?> objectList : objects) {
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
        if (objects != null) {
            for (DBObjectList<?> objectList : objects) {
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


    /*************************************************************
     *             Object Lists -  factory utilities             *
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
    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            @NotNull DBObjectType objectType,
            @NotNull BrowserTreeNode treeParent,
            GenericDatabaseElement sourceContentHolder,
            DynamicContentType<?> sourceContentType,
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
        DBObjectList<T> objectList = new DBObjectListImpl<>(objectType, treeParent, dependencyAdapter, statuses);
        addObjectList(objectList);

        return objectList;
    }

    public void addObjectList(DBObjectList<?> objectList) {
        if (objectList != null) {
            // enum map holds an array of all enum elements!!
            if (objects == null)
                objects = new DBObjectList[1]; else
                objects =  Arrays.copyOf(objects, objects.length + 1);

            objects[objects.length-1] = objectList;
        }
    }

    public void reloadObjects() {
        if (objects != null)  {
            for (DBObjectList<?> objectList : objects) {
                objectList.reload();
                checkDisposed();
            }
        }
    }

    public void refreshObjects() {
        if (objects != null)  {
            for (DBObjectList<?> objectList : objects) {
                if (check(objectList)) {
                    objectList.refresh();
                    checkDisposed();
                }
            }
        }
    }

    public void loadObjects() {
        if (objects != null)  {
            for (DBObjectList<?> objectList : objects) {
                if (!objectList.isInternal()) {
                    objectList.ensure();
                }
                checkDisposed();
            }
        }
    }

    public void loadObjects(DBObjectType objectType) {
        DBObjectList<?> objectList = getObjects(objectType);
        if (objectList == null) objectList = getInternalObjects(objectType);
        if (objectList != null) {
            objectList.getElements();
        }
    }

    /*****************************************************************
     *                      Object Relation Lists                    *
     *****************************************************************/
    private boolean isSupported(DBObjectRelationType objectRelationType) {
        ConnectionHandler connectionHandler = getOwner().getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connectionHandler);
        DatabaseObjectTypeId sourceTypeId = objectRelationType.getSourceType().getTypeId();
        DatabaseObjectTypeId targetTypeId = objectRelationType.getTargetType().getTypeId();
        return compatibilityInterface.supportsObjectType(sourceTypeId) &&
                compatibilityInterface.supportsObjectType(targetTypeId);
    }

    @Nullable
    public <T extends DBObjectRelation> DBObjectRelationList<T> getObjectRelations(DBObjectRelationType relationType) {
        if (objectRelations != null) {
            for (DBObjectRelationList objectRelations : objectRelations) {
                if (objectRelations.getObjectRelationType() == relationType) {
                    return objectRelations;
                }
            }
        }
        return null;
    }

    @Nullable
    public DBObjectRelationList createObjectRelationList(
            DBObjectRelationType type,
            GenericDatabaseElement parent,
            DBObjectList firstContent,
            DBObjectList secondContent) {
        if (isSupported(type)) {
            ContentDependencyAdapter dependencyAdapter = new DualContentDependencyAdapter(firstContent, secondContent);
            return createObjectRelationList(type, parent, dependencyAdapter);
        }
        return null;
    }

    public DBObjectRelationList createSubcontentObjectRelationList(
            DBObjectRelationType relationType,
            GenericDatabaseElement parent,
            DBObject sourceContentObject) {
        if (isSupported(relationType)) {
            ContentDependencyAdapter dependencyAdapter = new SubcontentDependencyAdapterImpl(sourceContentObject, relationType);
            return createObjectRelationList(relationType, parent, dependencyAdapter);
        }
        return null;
    }


    private DBObjectRelationList createObjectRelationList(
            DBObjectRelationType type,
            GenericDatabaseElement parent,
            ContentDependencyAdapter dependencyAdapter) {
        if (isSupported(type)) {
            DBObjectRelationList objectRelationList = new DBObjectRelationListImpl(type, parent, dependencyAdapter);

            if (objectRelations == null)
                objectRelations = new DBObjectRelationList[1]; else
                objectRelations =  Arrays.copyOf(objectRelations, objectRelations.length + 1);

            objectRelations[objectRelations.length-1] = objectRelationList;
            return objectRelationList;
        }
        return null;
    }

    /*****************************************************************
     *                      Disposable
     *****************************************************************/


    @Override
    public boolean isDisposed() {
        return objects == DISPOSED_OBJECTS || objectRelations == DISPOSED_OBJECT_RELATIONS;
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            DBObjectList<?>[] elements = this.objects;
            this.objects = DISPOSED_OBJECTS;
            this.objectRelations = DISPOSED_OBJECT_RELATIONS;
            this.owner = null;

            SafeDisposer.dispose(elements, false, false);
        }
    }
}
