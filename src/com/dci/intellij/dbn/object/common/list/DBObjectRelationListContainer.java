package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.MultipleContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapterImpl;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class DBObjectRelationListContainer implements StatefulDisposable {
    private static final DBObjectRelationList[] DISPOSED_CONTENT = new DBObjectRelationList[0];

    private GenericDatabaseElement owner;
    private DBObjectRelationList[] elements;

    public DBObjectRelationListContainer(GenericDatabaseElement owner) {
        this.owner = owner;
    }

    @Nullable
    public DBObjectRelationList[] getElements() {
        return elements;
    }

    private boolean isSupported(DBObjectRelationType objectRelationType) {
        ConnectionHandler connectionHandler = getOwner().getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connectionHandler);
        DatabaseObjectTypeId sourceTypeId = objectRelationType.getSourceType().getTypeId();
        DatabaseObjectTypeId targetTypeId = objectRelationType.getTargetType().getTypeId();
        return compatibilityInterface.supportsObjectType(sourceTypeId) &&
                compatibilityInterface.supportsObjectType(targetTypeId);
    }

    GenericDatabaseElement getOwner() {
        return Failsafe.nn(owner);
    }

    @Nullable
    public DBObjectRelationList getObjectRelationList(DBObjectRelationType objectRelationType) {
        if (elements != null) {
            for (DBObjectRelationList objectRelationList : elements) {
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

            if (elements == null)
                elements = new DBObjectRelationList[1]; else
                elements =  Arrays.copyOf(elements, elements.length + 1);

            elements[elements.length-1] = objectRelationList;
            return objectRelationList;
        }
        return null;
    }


    @Override
    public boolean isDisposed() {
        return elements == DISPOSED_CONTENT;
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            DBObjectRelationList[] objectRelationLists = this.elements;
            this.elements = DISPOSED_CONTENT;
            this.owner = null;

            SafeDisposer.dispose(objectRelationLists, false, false);
        }
    }

    public void reload() {
        if (elements != null) {
            for (DBObjectRelationList objectRelationList : elements) {
                objectRelationList.reload();
            }
        }
    }
}
