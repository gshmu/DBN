package com.dci.intellij.dbn.navigation.object;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.AsyncTaskExecutor;
import com.dci.intellij.dbn.common.thread.ThreadPool;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;
import com.dci.intellij.dbn.object.type.DBObjectType;

import java.util.List;
import java.util.concurrent.TimeUnit;

class DBObjectLookupScanner extends StatefulDisposable.Base implements DBObjectListVisitor {
    private final DBObjectLookupModel model;
    private final boolean forceLoad;
    private final AsyncTaskExecutor asyncScanner = new AsyncTaskExecutor(
            ThreadPool.objectLookupExecutor(), 3, TimeUnit.SECONDS);

    DBObjectLookupScanner(DBObjectLookupModel model, boolean forceLoad) {
        this.model = model;
        this.forceLoad = forceLoad;
    }

    @Override
    public void visit(DBObjectList<?> objectList) {
        if (isScannable(objectList)) {
            boolean sync = objectList.isLoaded();
            if (!sync) {
                BrowserTreeNode parent = objectList.getParent();
                if (parent instanceof DBObject) {
                    DBObject object = (DBObject) parent;
                    if (object.getParentObject() instanceof DBSchema) {
                        sync = true;
                    }
                }
            }

            if (sync) {
                doVisit(objectList);
            } else {
                asyncScanner.submit(() -> doVisit(objectList));
            }
        }
    }

    private void doVisit(DBObjectList<?> objectList) {
        DBObjectType objectType = objectList.getObjectType();
        boolean lookupEnabled = model.isObjectLookupEnabled(objectType);
        for (DBObject object : objectList.getObjects()) {
            checkDisposed();

            if (lookupEnabled) {
                model.getData().accept(object);
            }

            DBObjectListContainer childObjects = object.getChildObjects();
            if (childObjects != null) childObjects.visitObjects(this, true);
        }
    }

    public void scan() {
        ConnectionHandler selectedConnection = model.getSelectedConnection();
        DBSchema selectedSchema = model.getSelectedSchema();

        if (selectedConnection == null || selectedConnection.isVirtual()) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(model.getProject());
            List<ConnectionHandler> connectionHandlers = connectionManager.getConnections();
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                model.checkCancelled();

                DBObjectListContainer objectListContainer = connectionHandler.getObjectBundle().getObjectLists();
                objectListContainer.visitObjects(this, true);
            }
        } else {
            DBObjectListContainer objectListContainer =
                    selectedSchema == null ?
                            selectedConnection.getObjectBundle().getObjectLists() :
                            selectedSchema.getChildObjects();
            if (objectListContainer != null) {
                objectListContainer.visitObjects(this, true);
            }
        }
        asyncScanner.awaitCompletion();
    }

    private boolean isScannable(DBObjectList<?> objectList) {
        if (objectList != null) {
            DBObjectType objectType = objectList.getObjectType();
            if (model.isListLookupEnabled(objectType)) {
                if (objectType.isRootObject() || objectList.isInternal()) {
                    if (objectList.isLoaded()) {
                        return true;
                    } else {
                        // todo touch?
                    }
                }

                if (objectType.isSchemaObject() && objectList.getParentEntity() instanceof DBSchema) {
                    if (objectList.isLoaded()) {
                        return true;
                    } else {
                        // todo touch?
                    }
                }

/*
                if (objectList.isLoaded() || objectList.canLoadFast() || forceLoad) {
                    return true;
                }
*/
            }
        }
        return false;
    }

    @Override
    public void checkDisposed() {
        super.checkDisposed();
        model.checkCancelled();
    }

    @Override
    protected void disposeInner() {
        nullify();
    }
}
