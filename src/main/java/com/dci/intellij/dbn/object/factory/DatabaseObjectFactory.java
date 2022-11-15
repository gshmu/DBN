package com.dci.intellij.dbn.object.factory;

import com.dci.intellij.dbn.common.component.Components;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.interfaces.DatabaseDataDefinitionInterface;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputDialog;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.message.MessageCallback.when;

public class DatabaseObjectFactory extends ProjectComponentBase {

    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseObjectFactoryManager";

    private DatabaseObjectFactory(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static DatabaseObjectFactory getInstance(@NotNull Project project) {
        return Components.projectService(project, DatabaseObjectFactory.class);
    }

    private void notifyFactoryEvent(ObjectFactoryEvent event) {
        DBSchemaObject object = event.getObject();
        int eventType = event.getEventType();
        Project project = getProject();
        if (eventType == ObjectFactoryEvent.EVENT_TYPE_CREATE) {
            ProjectEvents.notify(project,
                    ObjectFactoryListener.TOPIC,
                    (listener) -> listener.objectCreated(object));

        } else if (eventType == ObjectFactoryEvent.EVENT_TYPE_DROP) {
            ProjectEvents.notify(project,
                    ObjectFactoryListener.TOPIC,
                    (listener) -> listener.objectDropped(object));
        }
    }


    public void openFactoryInputDialog(DBSchema schema, DBObjectType objectType) {
        Project project = getProject();
        if (objectType.isOneOf(DBObjectType.FUNCTION, DBObjectType.PROCEDURE)) {
            ObjectFactoryInputDialog dialog = new ObjectFactoryInputDialog(project, schema, objectType);
            dialog.show();
        } else {
            Messages.showErrorDialog(project, "Operation not supported", "Creation of " + objectType.getListName() + " is not supported yet.");
        }
    }

    public boolean createObject(ObjectFactoryInput factoryInput) {
        Project project = getProject();
        List<String> errors = new ArrayList<>();
        factoryInput.validate(errors);
        if (errors.size() > 0) {
            StringBuilder buffer = new StringBuilder("Could not create " + factoryInput.getObjectType().getName() + ". Please correct following errors: \n");
            for (String error : errors) {
                buffer.append(" - ").append(error).append("\n");
            }
            Messages.showErrorDialog(project, buffer.toString());
            return false;
        }
        if (factoryInput instanceof MethodFactoryInput) {
            MethodFactoryInput methodFactoryInput = (MethodFactoryInput) factoryInput;
            DBSchema schema = methodFactoryInput.getSchema();
            try {
                ConnectionHandler connection = schema.getConnection();
                DBNConnection conn = connection.getMainConnection(SchemaId.from(schema));
                DatabaseDataDefinitionInterface dataDefinition = connection.getDataDefinitionInterface();
                dataDefinition.createMethod(methodFactoryInput, conn);
                DBObjectType objectType = methodFactoryInput.isFunction() ? DBObjectType.FUNCTION : DBObjectType.PROCEDURE;
                Failsafe.nn(schema.getChildObjectList(objectType)).reload();

                DBMethod method = (DBMethod) schema.getChildObject(objectType, factoryInput.getObjectName(), false);
                Failsafe.nn(method.getChildObjectList(DBObjectType.ARGUMENT)).reload();

                DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
                databaseFileSystem.connectAndOpenEditor(method, null, false, true);
                notifyFactoryEvent(new ObjectFactoryEvent(method, ObjectFactoryEvent.EVENT_TYPE_CREATE));
            } catch (SQLException e) {
                Messages.showErrorDialog(project, "Could not create " + factoryInput.getObjectType().getName() + ".", e);
                return false;
            }
        }

        return true;
    }

    public void dropObject(DBSchemaObject object) {
        Messages.showQuestionDialog(
                getProject(),
                "Drop object",
                "Are you sure you want to drop the " + object.getQualifiedNameWithType() + "?",
                Messages.OPTIONS_YES_NO, 0,
                option -> when(option == 0, () ->
                        ConnectionAction.invoke("dropping the object", false, object, action -> {
                            Project project = getProject();
                            DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
                            databaseFileManager.closeFile(object);

                            Progress.prompt(project, "Dropping " + object.getQualifiedNameWithType(), false, progress -> doDropObject(object));
                        })));

    }

    private void doDropObject(DBSchemaObject object) {
        try {
            ConnectionHandler connection = object.getConnection();
            DatabaseInterface.run(false, connection, conn -> {
                DBContentType contentType = object.getContentType();

                String objectName = object.getQualifiedName();
                String objectTypeName = object.getTypeName();
                DatabaseDataDefinitionInterface dataDefinition = object.getDataDefinitionInterface();
                DBObjectList<?> objectList = (DBObjectList<?>) object.getParent();
                if (contentType == DBContentType.CODE_SPEC_AND_BODY) {
                    DBObjectStatusHolder objectStatus = object.getStatus();
                    if (objectStatus.is(DBContentType.CODE_BODY, DBObjectStatus.PRESENT)) {
                        dataDefinition.dropObjectBody(objectTypeName, objectName, conn);
                    }

                    if (objectStatus.is(DBContentType.CODE_SPEC, DBObjectStatus.PRESENT)) {
                        dataDefinition.dropObject(objectTypeName, objectName, conn);
                    }

                } else {
                    dataDefinition.dropObject(objectTypeName, objectName, conn);
                }

                objectList.reload();
                notifyFactoryEvent(new ObjectFactoryEvent(object, ObjectFactoryEvent.EVENT_TYPE_DROP));
            });
        } catch (SQLException e) {
            String message = "Could not drop " + object.getQualifiedNameWithType() + ".";
            Project project = getProject();
            Messages.showErrorDialog(project, message, e);
        }
    }
}