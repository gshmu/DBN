package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.MultipleContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapterImpl;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.Disposable;
import gnu.trove.THashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DBObjectListContainer implements Disposable {
    private Map<DBObjectType, DBObjectList<DBObject>> objectLists;
    private Map<DBObjectType, DBObjectList<DBObject>> hiddenObjectLists;
    private GenericDatabaseElement owner;

    public DBObjectListContainer(GenericDatabaseElement owner) {
        this.owner = owner;
    }
    
    public Collection<DBObjectList<DBObject>> getObjectLists() {
        return objectLists == null ? null : objectLists.values();
    }

    public void visitLists(DBObjectListVisitor visitor, boolean visitHidden) {
        if (objectLists != null) {
            for (DBObjectList<DBObject> objectList : objectLists.values()) {
                visitor.visitObjectList(objectList);
            }
        }
        if (visitHidden && hiddenObjectLists != null) {
            for (DBObjectList<DBObject> objectList : hiddenObjectLists.values()) {
                visitor.visitObjectList(objectList);
            }
        }
    }

    public DBObjectList getObjectList(DBObjectType objectType) {
        return objectLists == null ? null : objectLists.get(objectType);
    }

    public DBObjectList getHiddenObjectList(DBObjectType objectType) {
        return hiddenObjectLists == null ? null : hiddenObjectLists.get(objectType);
    }


    public DBObject getObject(DBObjectType objectType, String name) {
        DBObjectList objectList = getObjectList(objectType);
        if (objectList != null) {
            return objectList.getObject(name);
        }

        if (objectType.getInheritingTypes().size() > 0) {
            Set<DBObjectType> objectTypes = objectType.getInheritingTypes();
            for (DBObjectType objType : objectTypes) {
                DBObject object = getObject(objType, name);
                if (object != null) {
                    return object;
                }
            }
        }

        return null;
    }

    public <T extends DBObject> T getHiddenObject(DBObjectType objectType, String name) {
        if (objectType.isGeneric()) {
            Set<DBObjectType> objectTypes = objectType.getInheritingTypes();
            for (DBObjectType objType : objectTypes) {
                DBObjectList<T> objectList = getHiddenObjectList(objType);
                if (objectList != null) {
                    T object = objectList.getObject(name);
                    if (object != null) {
                        return object;
                    }
                }
            }
        } else {
            DBObjectList<T> objectList = getHiddenObjectList(objectType);
            if (objectList != null) {
                return objectList.getObject(name);
            }
        }
        return null;
    }

    public DBObject getObject(String name) {
        for (DBObjectList objectList : objectLists.values()) {
            DBObject object = objectList.getObject(name);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    public DBObject getHiddenObject(String name) {
        for (DBObjectList objectList : hiddenObjectLists.values()) {
            DBObject object = objectList.getObject(name);
            if (object != null) {
                return object;
            }
        }
        return null;
    }


    public DBObject getObjectForParentType(DBObjectType parentObjectType, String name, boolean lookupHidden) {
        for (DBObjectList objectList : objectLists.values()) {
            DBObjectType objectType = objectList.getObjectType();
            if (objectType.getParents().contains(parentObjectType)) {
                DBObject object = objectList.getObject(name);
                if (object != null) {
                    return object;
                }
            }
        }

        if (hiddenObjectLists != null && lookupHidden) {
            for (DBObjectList objectList : hiddenObjectLists.values()) {
                DBObjectType objectType = objectList.getObjectType();
                if (objectType.getParents().contains(parentObjectType)) {
                    DBObject object = objectList.getObject(name);
                    if (object != null) {
                        return object;
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

    public DBObject getObjectNoLoad(String name) {
        for (DBObjectList objectList : objectLists.values()) {
            if (objectList.isLoaded() && !objectList.isDirty()) {
                DBObject object = objectList.getObject(name);
                if (object != null && object.getParentObject().equals(owner)) {
                    return object;
                }
            }
        }
        return null;

    }

     public <T extends DBObject> DBObjectList<T>  createObjectList(
             DBObjectType objectType,
             BrowserTreeNode treeParent,
             DynamicContentLoader loader,
             boolean indexed,
             boolean hidden) {
        if (isSupported(objectType)) {
            ContentDependencyAdapter dependencyAdapter = new BasicDependencyAdapter(treeParent.getConnectionHandler());
            return createObjectList(objectType, treeParent, loader, dependencyAdapter, indexed, hidden);
        }
        return null;
    }

    public <T extends DBObject> DBObjectList<T>  createObjectList(
            DBObjectType objectType,
            BrowserTreeNode treeParent,
            DynamicContentLoader loader,
            DBObjectList[] sourceContents,
            boolean indexed, boolean hidden) {
        if (isSupported(objectType)) {
            ContentDependencyAdapter dependencyAdapter = new MultipleContentDependencyAdapter(treeParent.getConnectionHandler(), sourceContents);
            return createObjectList(objectType, treeParent, loader, dependencyAdapter, indexed, hidden);
        }
        return null;
    }

    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            DBObjectType objectType,
            BrowserTreeNode treeParent,
            DynamicContentLoader loader,
            GenericDatabaseElement sourceContentHolder,
            DynamicContentType sourceContentType,
            boolean indexed) {
        if (isSupported(objectType)) {
            if (sourceContentHolder.getDynamicContent(sourceContentType) != null) {
                ContentDependencyAdapter dependencyAdapter =
                        new SubcontentDependencyAdapterImpl(
                                sourceContentHolder,
                                sourceContentType
                        );
                return createObjectList(objectType, treeParent, loader, dependencyAdapter, indexed, false);
            }
        }
        return null;
    }

    public <T extends DBObject> DBObjectList<T> createSubcontentObjectList(
            DBObjectType objectType,
            BrowserTreeNode treeParent,
            DynamicContentLoader loader,
            DBObject sourceContentHolder,
            boolean indexed) {
        if (isSupported(objectType)) {
            if (sourceContentHolder.getDynamicContent(objectType) != null) {
                ContentDependencyAdapter dependencyAdapter =
                        new SubcontentDependencyAdapterImpl(
                                sourceContentHolder,
                                objectType
                        );
                return createObjectList(objectType, treeParent, loader, dependencyAdapter, indexed, false);
            }
        }
        return null;
    }

    private <T extends DBObject> DBObjectList<T> createObjectList(
            DBObjectType objectType,
            BrowserTreeNode treeParent,
            DynamicContentLoader<T> loader,
            ContentDependencyAdapter dependencyAdapter,
            boolean indexed,
            boolean hidden) {
        DBObjectList<T> objectList = new DBObjectListImpl<T>(objectType, treeParent, loader, dependencyAdapter, indexed);
        objectList.setHidden(hidden);
        addObjectList(objectList);

        return objectList;
    }

    public void addObjectList(DBObjectList objectList) {
        if (objectList != null) {
            DBObjectType objectType = objectList.getObjectType();
            if (objectList.isHidden()) {
                if (hiddenObjectLists == null) hiddenObjectLists = new THashMap<DBObjectType, DBObjectList<DBObject>>();
                hiddenObjectLists.put(objectType, objectList);
            } else {
                if (objectLists == null) objectLists =  new THashMap<DBObjectType, DBObjectList<DBObject>>();
                objectLists.put(objectType, objectList);
            }
        }
    }

    public void dispose() {
        DisposerUtil.dispose(objectLists);
        DisposerUtil.dispose(hiddenObjectLists);
        owner = null;

    }

    public void reload(boolean recursive) {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists.values()) {
                objectList.reload();
            }
        }
        if (hiddenObjectLists != null)  {
            for (DBObjectList objectList : hiddenObjectLists.values()) {
                objectList.reload();
            }
        }
    }

    public void load() {
        if (objectLists != null)  {
            for (DBObjectList objectList : objectLists.values()) {
                objectList.load(false);
            }
        }
        if (hiddenObjectLists != null)  {
            for (DBObjectList objectList : hiddenObjectLists.values()) {
                if (objectList.getObjectType() != DBObjectType.ANY) {
                    objectList.load(false);
                }
            }
        }
    }
}
