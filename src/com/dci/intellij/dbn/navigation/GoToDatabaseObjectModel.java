package com.dci.intellij.dbn.navigation;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.VirtualConnectionHandler;
import com.dci.intellij.dbn.navigation.options.ObjectsLookupSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoToDatabaseObjectModel extends StatefulDisposable.Base implements ChooseByNameModel {
    private final ProjectRef project;
    private final ConnectionHandlerRef selectedConnection;
    private final DBObjectRef<DBSchema> selectedSchema;
    private final ObjectsLookupSettings objectsLookupSettings;
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];


    public GoToDatabaseObjectModel(@NotNull Project project, @Nullable ConnectionHandler selectedConnection, DBSchema selectedSchema) {
        this.project = ProjectRef.of(project);
        this.selectedConnection = ConnectionHandlerRef.from(selectedConnection);
        this.selectedSchema = DBObjectRef.of(selectedSchema);
        objectsLookupSettings = ProjectSettingsManager.getSettings(project).getNavigationSettings().getObjectsLookupSettings();
    }

    @Override
    public String getPromptText() {
        ConnectionHandler selectedConnection = getSelectedConnection();
        String connectionIdentifier = selectedConnection == null || selectedConnection instanceof VirtualConnectionHandler ?
                "All Connections" :
                selectedConnection.getName();
        return "Enter database object name (" + connectionIdentifier + (selectedSchema == null ? "" : " / " + selectedSchema.objectName) + ")";
    }

    private ConnectionHandler getSelectedConnection() {
        return selectedConnection.get();
    }

    @NotNull
    @Override
    public String getNotInMessage() {
        return "No database object matching criteria";
    }

    @NotNull
    @Override
    public String getNotFoundMessage() {
        return "Database object not found";
    }

    @Override
    public String getCheckBoxName() {
        return objectsLookupSettings.getForceDatabaseLoad().value() ? "Load database objects" : null;
    }

    @Override
    public char getCheckBoxMnemonic() {
        return 0;
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {
    }

    @NotNull
    @Override
    public ListCellRenderer getListCellRenderer() {
        return DatabaseObjectListCellRenderer.INSTANCE;
    }

    @Override
    public boolean willOpenEditor() {
        return false;
    }

    @Override
    public boolean useMiddleMatching() {
        return true;
    }

    @Override
    @NotNull
    public String[] getNames(boolean checkBoxState) {
        try {
            boolean databaseLoadActive = objectsLookupSettings.getForceDatabaseLoad().value();
            boolean forceLoad = checkBoxState && databaseLoadActive;

            if (!forceLoad && selectedSchema != null) {
                // touch the schema for next load
                selectedSchema.ensure().getChildren();
            }
            checkDisposed();
            ProgressMonitor.checkCancelled();

            ObjectNamesCollector collector = new ObjectNamesCollector(forceLoad);
            scanObjectLists(collector);

            Set<String> bucket = collector.getBucket();
            if (bucket != null) {
                return bucket.toArray(new String[0]);
            }
        } catch (ProcessCanceledException ignore) {}

        return EMPTY_STRING_ARRAY;
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    @Override
    @NotNull
    public Object[] getElementsByName(@NotNull String name, boolean checkBoxState, @NotNull String pattern) {
        try {
            boolean forceLoad = checkBoxState && objectsLookupSettings.getForceDatabaseLoad().value();
            checkDisposed();
            ProgressMonitor.checkCancelled();

            ObjectCollector collector = new ObjectCollector(name, forceLoad);
            scanObjectLists(collector);
            List<DBObject> bucket = collector.getBucket();
            if (bucket != null) {
                return bucket.toArray();
            }
        } catch (ProcessCanceledException ignore) {}

        return EMPTY_ARRAY;
    }

    private void scanObjectLists(DBObjectListVisitor visitor) {
        ConnectionHandler selectedConnection = getSelectedConnection();
        if (selectedConnection == null || selectedConnection instanceof VirtualConnectionHandler) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
            List<ConnectionHandler> connectionHandlers = connectionManager.getConnectionHandlers();
            CollectionUtil.forEach(
                    connectionHandlers,
                    (connectionHandler -> {
                        checkDisposed();
                        ProgressMonitor.checkCancelled();

                        DBObjectListContainer objectListContainer = connectionHandler.getObjectBundle().getObjectListContainer();
                        objectListContainer.visitLists(visitor, false);
                    }));
        } else {
            DBSchema schema = DBObjectRef.get(selectedSchema);
            DBObjectListContainer objectListContainer =
                    schema == null ?
                            selectedConnection.getObjectBundle().getObjectListContainer() :
                            schema.getChildObjects();
            if (objectListContainer != null) {
                objectListContainer.visitLists(visitor, false);
            }
        }
    }

    private class ObjectNamesCollector extends Base implements DBObjectListVisitor {
        private final boolean forceLoad;
        private DBObject parentObject;
        private Set<String> bucket;

        private ObjectNamesCollector(boolean forceLoad) {
            this.forceLoad = forceLoad;
        }

        @Override
        public void visit(DBObjectList<DBObject> objectList) {
            if (isListScannable(objectList) && isParentRelationValid(objectList)) {
                DBObjectType objectType = objectList.getObjectType();
                if (isLookupEnabled(objectType)) {
                    boolean isLookupEnabled = objectsLookupSettings.isEnabled(objectType);
                    DBObject originalParentObject = parentObject;
                    try {
                        CollectionUtil.forEach(
                                objectList.getElements(),
                                (object) -> {
                                    checkDisposed();
                                    ProgressMonitor.checkCancelled();

                                    if (isLookupEnabled) {
                                        if (bucket == null) bucket = new HashSet<>();
                                        bucket.add(object.getName());
                                    }

                                    parentObject = object;
                                    DBObjectListContainer childObjects = object.getChildObjects();
                                    if (childObjects != null) childObjects.visitLists(this, false);
                                }
                        );
                    } finally {
                        parentObject = originalParentObject;
                    }
                }
            }
        }

        private boolean isListScannable(DBObjectList<DBObject> objectList) {
            return objectList != null && (objectList.isLoaded() || objectList.canLoadFast() || forceLoad);
        }

        private boolean isParentRelationValid(DBObjectList<DBObject> objectList) {
            return parentObject == null || objectList.getObjectType().isChildOf(parentObject.getObjectType());
        }

        public Set<String> getBucket() {
            return bucket;
        }

        @Override
        protected void disposeInner() {
            nullify();
        }
    }

    private boolean isLookupEnabled(DBObjectType objectType) {
        boolean enabled = objectsLookupSettings.isEnabled(objectType);
        if (!enabled) {
            for (DBObjectType childObjectType : objectType.getChildren()) {
                if (isLookupEnabled(childObjectType)) {
                    return true;
                }
            }
        }
        return enabled;
    }


    private class ObjectCollector extends Base implements DBObjectListVisitor {
        private final String objectName;
        private final boolean forceLoad;
        private DBObject parentObject;
        private List<DBObject> bucket;

        private ObjectCollector(String objectName, boolean forceLoad) {
            this.objectName = objectName;
            this.forceLoad = forceLoad;
        }

        @Override
        public void visit(DBObjectList<DBObject> objectList) {
            if (isListScannable(objectList) && isParentRelationValid(objectList)) {
                DBObjectType objectType = objectList.getObjectType();
                if (isLookupEnabled(objectType)) {
                    boolean isLookupEnabled = objectsLookupSettings.isEnabled(objectType);
                    DBObject originalParentObject = parentObject;
                    try {
                        CollectionUtil.forEach(
                                objectList.getObjects(),
                                (object) -> {
                                    checkDisposed();
                                    ProgressMonitor.checkCancelled();

                                    if (isLookupEnabled && object.getName().equals(objectName)) {
                                        if (bucket == null) bucket = new ArrayList<>();
                                        bucket.add(object);
                                    }

                                    parentObject = object;
                                    DBObjectListContainer childObjects = object.getChildObjects();
                                    if (childObjects != null) childObjects.visitLists(this, false);
                                });
                    } finally {
                        parentObject = originalParentObject;
                    }
                }
            }
        }

        private boolean isListScannable(DBObjectList<DBObject> objectList) {
            return objectList != null && (objectList.isLoaded() || objectList.canLoadFast() || forceLoad);
        }

        private boolean isParentRelationValid(DBObjectList<DBObject> objectList) {
            return parentObject == null || objectList.getObjectType().isChildOf(parentObject.getObjectType());
        }

        public List<DBObject> getBucket() {
            return bucket;
        }

        @Override
        protected void disposeInner() {
            nullify();
        }
    }


    @Override
    public String getElementName(@NotNull Object element) {
        if (element instanceof DBObject) {
            DBObject object = (DBObject) element;
            return object.getQualifiedName();
        }

        return element.toString();
    }

    @Override
    @NotNull
    public String[] getSeparators() {
        return new String[]{"."};
    }

    @Override
    public String getFullName(@NotNull Object element) {
        return getElementName(element);
    }

    @Override
    public String getHelpId() {
        return null;
    }

    public static class DatabaseObjectListCellRenderer extends ColoredListCellRenderer {
        private static final DatabaseObjectListCellRenderer INSTANCE = new DatabaseObjectListCellRenderer();

        @Override
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            if (value instanceof DBObject) {
                DBObject object = (DBObject) value;
                setIcon(object.getIcon());
                append(object.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                ConnectionHandler connectionHandler = Failsafe.nn(object.getConnectionHandler());
                append(" [" + connectionHandler.getName() + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);
                if (object.getParentObject() != null) {
                    append(" - " + object.getParentObject().getQualifiedName(), SimpleTextAttributes.GRAY_ATTRIBUTES);
                }
            } else append(value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }

    @Override
    protected void disposeInner() {
        nullify();
    }
}
