package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.MultipleContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapterImpl;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.Compactable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBObjectRelationListContainer implements Disposable, Compactable {
    private GenericDatabaseElement owner;
    private List<DBObjectRelationList> objectRelationLists;

    public DBObjectRelationListContainer(GenericDatabaseElement owner) {
        this.owner = owner;
    }

    @Override
    public void compact() {
        CollectionUtil.compactRecursive(objectRelationLists);
    }

    public List<DBObjectRelationList> getObjectRelationLists() {
        return objectRelationLists;
    }

    private boolean isSupported(DBObjectRelationType objectRelationType) {
        ConnectionHandler connectionHandler = owner.getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connectionHandler);
        DatabaseObjectTypeId sourceTypeId = objectRelationType.getSourceType().getTypeId();
        DatabaseObjectTypeId targetTypeId = objectRelationType.getTargetType().getTypeId();
        return compatibilityInterface.supportsObjectType(sourceTypeId) &&
                compatibilityInterface.supportsObjectType(targetTypeId);
    }

    @Nullable
    public DBObjectRelationList getObjectRelationList(DBObjectRelationType objectRelationType) {
        if (objectRelationLists != null) {
            for (DBObjectRelationList objectRelationList : objectRelationLists) {
                if (objectRelationList.getObjectRelationType() == objectRelationType) {
                    return objectRelationList;
                }
            }
        }
        return null;
    }

    @Nullable
    public DBObjectRelationList createObjectRelationList(
            DBObjectRelationType type,
            GenericDatabaseElement parent,
            DBObjectList... sourceContents) {
        if (isSupported(type)) {
            ContentDependencyAdapter dependencyAdapter = new MultipleContentDependencyAdapter(sourceContents);
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
            if (objectRelationLists == null) objectRelationLists = new ArrayList<>();
            objectRelationLists.add(objectRelationList);
            return objectRelationList;
        }
        return null;
    }

    @Override
    public void dispose() {
        Disposer.dispose(objectRelationLists);
        CollectionUtil.clear(objectRelationLists);
        owner = null;
    }

    public void reload() {
        for (DBObjectRelationList objectRelationList : objectRelationLists) {
            objectRelationList.reload();
        }        
    }
}
