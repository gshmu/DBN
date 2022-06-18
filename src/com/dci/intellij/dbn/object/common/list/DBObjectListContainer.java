package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Direction;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.DualContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapter;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.Direction.DOWN;
import static com.dci.intellij.dbn.common.Direction.UP;
import static com.dci.intellij.dbn.common.dispose.Failsafe.check;
import static com.dci.intellij.dbn.common.util.Search.binarySearch;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;
import static com.dci.intellij.dbn.object.type.DBObjectType.ANY;
import static java.util.Collections.emptyList;

@Getter
public final class DBObjectListContainer implements StatefulDisposable {
    private static final DBObjectList<?>[] DISPOSED_OBJECTS = new DBObjectList[0];
    private static final DBObjectRelationList[] DISPOSED_RELATIONS = new DBObjectRelationList[0];

    private DatabaseEntity owner;
    private DBObjectList<?>[] objects;
    private DBObjectRelationList[] relations;

    public DBObjectListContainer(@NotNull DatabaseEntity owner) {
        this.owner = owner;
    }

    @NotNull
    private DatabaseEntity getOwner() {
        return Failsafe.nn(owner);
    }

    public void visitObjects(@NotNull DBObjectListVisitor visitor, boolean visitInternal) {
        if (objects != null) {
            try {
                checkDisposed(visitor);
                for (DBObjectList<?> objectList : objects) {
                    if (check(objectList) && (visitInternal || !objectList.isInternal())) {
                        checkDisposed(visitor);
                        ProgressMonitor.checkCancelled();

                        visitor.visit(objectList);
                    }
                }
            } catch (ProcessCanceledException ignore) {}
        }
    }

    private void checkDisposed(DBObjectListVisitor visitor) {
        Failsafe.nd(this);
        Failsafe.nd(visitor);
    }

    @NotNull
    public <T extends DBObject> List<T> getObjects(DBObjectType objectType, boolean internal) {
        DBObjectList<?> objects = getObjectList(objectType, internal);
        if (objects == null) {
            return emptyList();
        } else {
            return cast(objects.getObjects());
        }
    }

    @Nullable
    private <T extends DBObject> DBObjectList<T> findObjects(DBObjectType objectType) {
        return cast(binarySearch(objects, o -> o.getObjectType().compareTo(objectType)));
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> getObjectList(DBObjectType objectType) {
        return getObjectList(objectType, false);
    }

    public <T extends DBObject> DBObjectList<T> getObjectList(DBObjectType objectType, boolean internal) {
        DBObjectList<T> objectList = findObjects(objectType);
        if (check(objectList) && internal == objectList.isInternal()) {
            return objectList;
        }
        return null;
    }

    public <T extends DBObject> T getObject(DBObjectType objectType, String name, short overload) {
        return objectType == ANY  ?
                findAnyObject(name, overload, false) :
                findObject(objectType, name, overload, Direction.ANY, false);
    }

    public <T extends DBObject> T getInternalObject(DBObjectType objectType, String name, short overload) {
        return objectType == ANY  ?
                findAnyObject(name, overload, true) :
                findObject(objectType, name, overload, Direction.ANY, true);
    }

    @Nullable
    private <T extends DBObject> T findAnyObject(String name, short overload, boolean internal) {
        for (DBObjectList<?> objectList : getObjects()) {
            if (internal == objectList.isInternal() && !objectList.isDependency() && !objectList.isHidden()) {
                DBObject object = objectList.getObject(name, overload);
                if (object != null) {
                    return cast(object);
                }
            }
        }
        return null;
    }

    @Nullable
    private <T extends DBObject> T findObject(DBObjectType objectType, String name, short overload, Direction direction, boolean internal) {
        DBObjectList<?> objectList = getObjectList(objectType, internal);

        if (objectList != null && !objectList.isHidden()) {
            return cast(objectList.getObject(name, overload));
        }

        switch (direction) {
            case UP:   return findInheritedObject(objectType, name, overload, internal);
            case DOWN: return findInheritingObject(objectType, name, overload, internal);
            case ANY:  return Commons.coalesce(
                        () -> findInheritedObject(objectType, name, overload, internal),
                        () -> findInheritingObject(objectType, name, overload, internal));
        }
        return null;
    }

    @Nullable
    private <T extends DBObject> T findInheritedObject(DBObjectType objectType, String name, short overload, boolean internal) {
        DBObjectType inheritedType = objectType.getInheritedType();
        if (inheritedType != null && inheritedType != objectType) {
            return findObject(inheritedType, name, overload, UP, internal);
        }
        return null;
    }

    @Nullable
    private <T extends DBObject> T findInheritingObject(DBObjectType objectType, String name, short overload, boolean internal) {
        Set<DBObjectType> inheritingTypes = objectType.getInheritingTypes();
        if (!inheritingTypes.isEmpty()) {
            for (DBObjectType objType : inheritingTypes) {
                DBObject object = findObject(objType, name, overload, DOWN, internal);
                if (object != null) {
                    return cast(object);
                }
            }
        }
        return null;
    }

    @Nullable
    public DBObject getObjectForParentType(DBObjectType parentObjectType, String name, short overload, boolean lookupInternal) {
        if (objects != null) {
            for (DBObjectList<?> objectList : objects) {
                if (check(objectList) && !objectList.isHidden() && !objectList.isDependency()) {
                    if (lookupInternal || !objectList.isInternal()) {
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
        }

        return null;
    }

    private boolean isSupported(DBObjectType objectType) {
        DatabaseEntity owner = getOwner();
        ConnectionHandler connection = owner.getConnection();
        DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connection);
        return compatibilityInterface.supportsObjectType(objectType.getTypeId());
    }

    public DBObject getObjectNoLoad(String name, short overload) {
        if (objects != null) {
            for (DBObjectList<?> objectList : objects) {
                if (check(objectList) && objectList.isLoaded() && !objectList.isDirty()) {
                    DatabaseEntity owner = getOwner();
                    if (owner instanceof DBObject) {
                        DBObject object = objectList.getObject(name, overload);
                        if (object != null) {
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
            DatabaseEntity sourceContentHolder,
            DynamicContentType<?> sourceContentType,
            DynamicContentStatus... statuses) {
        if (isSupported(objectType)) {
            if (sourceContentHolder != null && sourceContentHolder.getDynamicContent(sourceContentType) != null) {
                ContentDependencyAdapter dependencyAdapter =
                        SubcontentDependencyAdapter.create(
                                sourceContentHolder,
                                sourceContentType);
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
                        SubcontentDependencyAdapter.create(
                                sourceContentHolder,
                                objectType);
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
            if (objects == null) {
                objects = new DBObjectList[]{objectList};
            } else {
                DBObjectList[] objects = new DBObjectList[this.objects.length + 1];
                int ordinal = objectList.getObjectType().ordinal();
                if (ordinal < this.objects[0].getObjectType().ordinal()) {
                    System.arraycopy(this.objects, 0, objects, 1, this.objects.length);
                    objects[0] = objectList;

                } else if (ordinal > this.objects[this.objects.length-1].getObjectType().ordinal()) {
                    System.arraycopy(this.objects, 0, objects, 0, this.objects.length);
                    objects[objects.length - 1] = objectList;

                } else {
                    boolean inserted = false;
                    for (int i = 0; i < this.objects.length; i++) {
                        int localOrdinal = this.objects[i].getObjectType().ordinal();
                        if (localOrdinal < ordinal) {
                            objects[i] = this.objects[i];
                        } else {
                            if (!inserted) {
                                objects[i] = objectList;
                                inserted = true;
                            }
                            objects[i + 1] = this.objects[i];
                        }
                    }
                }

                this.objects = objects;
            }
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
        DBObjectList<?> objectList = Commons.coalesce(
                () -> getObjectList(objectType, false),
                () -> getObjectList(objectType, true));

        if (objectList != null) {
            objectList.getElements();
        }
    }

    /*****************************************************************
     *                      Object Relation Lists                    *
     *****************************************************************/
    private boolean isSupported(DBObjectRelationType objectRelationType) {
        ConnectionHandler connection = getOwner().getConnection();
        DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connection);
        DatabaseObjectTypeId sourceTypeId = objectRelationType.getSourceType().getTypeId();
        DatabaseObjectTypeId targetTypeId = objectRelationType.getTargetType().getTypeId();
        return compatibilityInterface.supportsObjectType(sourceTypeId) &&
                compatibilityInterface.supportsObjectType(targetTypeId);
    }

    @Nullable
    public <T extends DBObjectRelation> DBObjectRelationList<T> getRelations(DBObjectRelationType relationType) {
        if (relations != null) {
            for (DBObjectRelationList objectRelations : relations) {
                if (objectRelations.getObjectRelationType() == relationType) {
                    return cast(objectRelations);
                }
            }
        }
        return null;
    }

    public void createObjectRelationList(
            DBObjectRelationType type,
            DatabaseEntity parent,
            DBObjectList firstContent,
            DBObjectList secondContent) {
        if (isSupported(type)) {
            ContentDependencyAdapter dependencyAdapter = DualContentDependencyAdapter.create(firstContent, secondContent);
            createObjectRelationList(type, parent, dependencyAdapter);
        }
    }

    public void createSubcontentObjectRelationList(
            DBObjectRelationType relationType,
            DatabaseEntity parent,
            DBObject sourceContentObject) {
        if (isSupported(relationType)) {
            ContentDependencyAdapter dependencyAdapter = SubcontentDependencyAdapter.create(sourceContentObject, relationType);
            createObjectRelationList(relationType, parent, dependencyAdapter);
        }
    }


    private void createObjectRelationList(
            DBObjectRelationType type,
            DatabaseEntity parent,
            ContentDependencyAdapter dependencyAdapter) {
        if (isSupported(type)) {
            DBObjectRelationList objectRelationList = new DBObjectRelationListImpl(type, parent, dependencyAdapter);

            if (relations == null)
                relations = new DBObjectRelationList[1]; else
                relations =  Arrays.copyOf(relations, relations.length + 1);

            relations[relations.length-1] = objectRelationList;
        }
    }

    /*****************************************************************
     *                      Disposable
     *****************************************************************/


    @Override
    public boolean isDisposed() {
        return objects == DISPOSED_OBJECTS || relations == DISPOSED_RELATIONS;
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            this.objects = SafeDisposer.replace(this.objects, DISPOSED_OBJECTS, false);
            this.relations = SafeDisposer.replace(this.relations, DISPOSED_RELATIONS, false);
            this.owner = null;
        }
    }
}
