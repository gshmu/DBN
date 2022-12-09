package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.Direction;
import com.dci.intellij.dbn.common.content.DynamicContentProperty;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.DualContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapter;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.dispose.UnlistedDisposable;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.dci.intellij.dbn.common.Direction.DOWN;
import static com.dci.intellij.dbn.common.Direction.UP;
import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;
import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;
import static com.dci.intellij.dbn.object.type.DBObjectType.ANY;
import static java.util.Collections.emptyList;

@Getter
public final class DBObjectListContainer implements StatefulDisposable, UnlistedDisposable {
    private static final DynamicContentTypeIndex<DBObjectType, DBObjectType> OBJECT_INDEX = new DynamicContentTypeIndex<>(DBObjectType.class);
    private static final DynamicContentTypeIndex<DBObjectType, DBObjectRelationType> RELATION_INDEX = new DynamicContentTypeIndex<>(DBObjectType.class);

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

    public void visit(DBObjectListVisitor visitor, boolean visitInternal) {
        if (objects == null) return;
        guarded(() -> {
            if (isNotValid(visitor)) return;
            for (DBObjectList<?> list : objects) {
                if (list == null) continue;
                if (list.isInternal() && !visitInternal) continue;
                if (isNotValid(list)) continue;
                if (isNotValid(visitor)) return;

                ProgressMonitor.checkCancelled();
                visitor.visit(list);
            }
        });
    }

    private void checkDisposed(DBObjectListVisitor visitor) {
        nd(this);
        nd(visitor);
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

    private DBObjectType getOwnerType() {
        if (owner instanceof DBObject) {
            DBObject object = (DBObject) owner;
            return object.getObjectType();
        } else if (owner instanceof DBObjectBundle) {
            return DBObjectType.BUNDLE;
        }

        throw new IllegalArgumentException();
    }

    private DatabaseType getDatabaseType() {
        return owner.getConnection().getDatabaseType();
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> getObjectList(DBObjectType objectType) {
        return getObjectList(objectType, false);
    }

    public <T extends DBObject> DBObjectList<T> getObjectList(DBObjectType objectType, boolean internal) {
        DBObjectList<T> objectList = objects(objectType);
        if (isValid(objectList) && internal == objectList.isInternal()) {
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
        if (objects == null) return null;

        for (DBObjectList<?> objectList : objects) {
            if (isNotValid(objectList) || objectList.isHidden() || objectList.isDependency()) continue;
            if (objectList.isInternal() && !lookupInternal) continue;

            DBObjectType objectType = objectList.getObjectType();
            if (!parentObjectType.isParentOf(objectType)) continue;

            DBObject object = objectList.getObject(name, overload);
            if (isNotValid(object)) continue;

            return object;
        }

        return null;
    }

    private boolean isSupported(DBObjectType objectType) {
        DatabaseEntity owner = getOwner();
        return objectType.isSupported(owner);
    }

    public DBObject getObjectNoLoad(String name, short overload) {
        if (objects == null) return null;

        for (DBObjectList<?> objectList : objects) {
            if (isNotValid(objectList) || !objectList.isLoaded() || objectList.isDirty())  continue;

            DBObject object = objectList.getObject(name, overload);
            if (isNotValid(object)) continue;

            DatabaseEntity owner = getOwner();
            if (owner.isObjectBundle()) {
                return object;
            }

            if (owner.isObject()) {
                DBObject ownerObject = (DBObject) owner;
                if (ownerObject.isParentOf(object)) {
                    return object;
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
            @NotNull DatabaseEntity treeParent,
            DynamicContentProperty... properties) {

        if (!isSupported(objectType)) return null;
        return createObjectList(objectType, treeParent, BasicDependencyAdapter.INSTANCE, properties);
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            @NotNull DBObjectType objectType,
            @NotNull DatabaseEntity treeParent,
            DatabaseEntity sourceContentHolder,
            DynamicContentType<?> sourceContentType,
            DynamicContentProperty... properties) {

        if (!isSupported(objectType) || sourceContentHolder == null) return null;

        val dynamicContent = sourceContentHolder.getDynamicContent(sourceContentType);
        if (dynamicContent == null) return null;

        ContentDependencyAdapter dependencyAdapter = SubcontentDependencyAdapter.create(sourceContentHolder, sourceContentType);
        return createObjectList(objectType, treeParent, dependencyAdapter, properties);
    }

    @Nullable
    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            @NotNull DBObjectType objectType,
            @NotNull DatabaseEntity treeParent,
            DBObject sourceContentHolder,
            DynamicContentProperty... properties) {

        if (!isSupported(objectType)) return null;

        val dynamicContent = sourceContentHolder.getDynamicContent(objectType);
        if (dynamicContent == null) return null;

        ContentDependencyAdapter dependencyAdapter = SubcontentDependencyAdapter.create(sourceContentHolder, objectType);
        return createObjectList(objectType, treeParent, dependencyAdapter, properties);
    }

    private <T extends DBObject> DBObjectList<T> createObjectList(
            @NotNull DBObjectType objectType,
            @NotNull DatabaseEntity parent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentProperty... properties) {

        boolean grouped = Commons.isOneOf(DynamicContentProperty.GROUPED, properties);
        DBObjectList<T> objectList = grouped ?
                new DBObjectListImpl.Grouped<>(objectType, parent, dependencyAdapter, properties):
                new DBObjectListImpl<>(objectType, parent, dependencyAdapter, properties);
        addObjectList(objectList);

        return objectList;
    }

    public void addObjectList(DBObjectList<?> objectList) {
        if (objectList != null) {
            addObjects(objectList);
        }
    }

    public void loadObjects() {
        visit(o -> o.load(), false);
    }

    public void reloadObjects() {
        visit(o -> o.reload(), true);
    }

    public void refreshObjects() {
        visit(o -> o.refresh(), true);
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
    private boolean isSupported(DBObjectRelationType relationType) {
        return isSupported(relationType.getSourceType()) &&
                isSupported(relationType.getTargetType());
    }

    @Nullable
    public <T extends DBObjectRelation> DBObjectRelationList<T> getRelations(DBObjectRelationType relationType) {
        if (relations == null) return null;
        for (DBObjectRelationList objectRelations : relations) {
            if (objectRelations.getRelationType() == relationType) {
                return cast(objectRelations);
            }
        }
        return null;
    }

    public void createObjectRelationList(
            DBObjectRelationType type,
            DatabaseEntity parent,
            DBObjectList firstContent,
            DBObjectList secondContent,
            DynamicContentProperty... properties) {

        if (!isSupported(type)) return;
        ContentDependencyAdapter dependencyAdapter = DualContentDependencyAdapter.create(firstContent, secondContent);
        createObjectRelationList(type, parent, dependencyAdapter, properties);
    }

    public void createSubcontentObjectRelationList(
            DBObjectRelationType relationType,
            DatabaseEntity parent,
            DBObject sourceContentObject,
            DynamicContentProperty... properties) {

        if (!isSupported(relationType)) return;
        ContentDependencyAdapter dependencyAdapter = SubcontentDependencyAdapter.create(sourceContentObject, relationType);
        createObjectRelationList(relationType, parent, dependencyAdapter, properties);
    }


    private void createObjectRelationList(
            DBObjectRelationType type,
            DatabaseEntity parent,
            ContentDependencyAdapter dependencyAdapter,
            DynamicContentProperty... properties) {

        if (!isSupported(type)) return;
        boolean grouped = Commons.isOneOf(DynamicContentProperty.GROUPED, properties);
        DBObjectRelationList relations = grouped ?
                new DBObjectRelationListImpl.Grouped(type, parent, dependencyAdapter, properties) :
                new DBObjectRelationListImpl(type, parent, dependencyAdapter, properties);
        addRelations(relations);
    }

    private void addObjects(DBObjectList objects) {
        if (objects == null) return;

        int index = objectsIndex(objects.getObjectType());
        int length = index + 1;

        if (this.objects == null)
            this.objects = new DBObjectList[length]; else
            this.objects = Arrays.copyOf(this.objects, length);

        this.objects[index] = objects;
    }

    private void addRelations(DBObjectRelationList relations) {
        int index = relationsIndex(relations.getRelationType());
        int length = index + 1;

        if (this.relations == null)
            this.relations = new DBObjectRelationList[length]; else
            this.relations = Arrays.copyOf(this.relations, length);

        this.relations[index] = relations;
    }

    private int objectsIndex(DBObjectType objectType) {
        return OBJECT_INDEX.index(
                getDatabaseType(),
                getOwnerType(),
                objectType);
    }

    private int relationsIndex(DBObjectRelationType relationType) {
        return RELATION_INDEX.index(
                getDatabaseType(),
                getOwnerType(),
                relationType);
    }

    @Nullable
    private <T extends DBObject> DBObjectList<T> objects(DBObjectType objectType) {
        if (objects != null && objects != DISPOSED_OBJECTS) {
            int index = objectsIndex(objectType);
            if (index < objects.length) {
                return cast(objects[index]);
            }
        }

        return null;
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
            this.objects = Disposer.replace(this.objects, DISPOSED_OBJECTS);
            this.relations = Disposer.replace(this.relations, DISPOSED_RELATIONS);
            this.owner = null;
        }
    }
}
