package com.dci.intellij.dbn.object.factory;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.factory.ui.FunctionFactoryInputForm;
import com.dci.intellij.dbn.object.factory.ui.ProcedureFactoryInputForm;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputDialog;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputForm;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class DatabaseObjectFactory extends AbstractProjectComponent {

    private DatabaseObjectFactory(Project project) {
        super(project);
    }

    public static DatabaseObjectFactory getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatabaseObjectFactory.class);
    }

    private void notifyFactoryEvent(ObjectFactoryEvent event) {
        DBSchemaObject object = event.getObject();
        int eventType = event.getEventType();
        Project project = getProject();
        if (eventType == ObjectFactoryEvent.EVENT_TYPE_CREATE) {
            EventUtil.notify(project, ObjectFactoryListener.TOPIC).objectCreated(object);
        } else if (eventType == ObjectFactoryEvent.EVENT_TYPE_DROP) {
            EventUtil.notify(project, ObjectFactoryListener.TOPIC).objectDropped(object);
        }
    }


    public void openFactoryInputDialog(DBSchema schema, DBObjectType objectType) {
        Project project = getProject();
        ObjectFactoryInputForm inputForm =
            objectType == DBObjectType.FUNCTION ? new FunctionFactoryInputForm(project, schema, objectType, 0) :
            objectType == DBObjectType.PROCEDURE ? new ProcedureFactoryInputForm(project, schema, objectType, 0) : null;

        if (inputForm == null) {
            MessageUtil.showErrorDialog(project, "Operation not supported", "Creation of " + objectType.getListName() + " is not supported yet.");
        } else {
            ObjectFactoryInputDialog dialog = new ObjectFactoryInputDialog(project, inputForm);
            dialog.show();
        }
    }

    public boolean createObject(ObjectFactoryInput factoryInput) {
        Project project = getProject();
        List<String> errors = new ArrayList<>();
        factoryInput.validate(errors);
        if (errors.size() > 0) {
            StringBuilder buffer = new StringBuilder("Could not instructions " + factoryInput.getObjectType().getName() + ". Please correct following errors: \n");
            for (String error : errors) {
                buffer.append(" - ").append(error).append("\n");
            }
            MessageUtil.showErrorDialog(project, buffer.toString());
            return false;
        }
        if (factoryInput instanceof MethodFactoryInput) {
            MethodFactoryInput methodFactoryInput = (MethodFactoryInput) factoryInput;
            DBSchema schema = methodFactoryInput.getSchema();
            try {
                ConnectionHandler connectionHandler = schema.getConnectionHandler();
                DBNConnection connection = connectionHandler.getMainConnection(SchemaId.from(schema));
                connectionHandler.getInterfaceProvider().getDDLInterface().createMethod(methodFactoryInput, connection);
                DBObjectType objectType = methodFactoryInput.isFunction() ? DBObjectType.FUNCTION : DBObjectType.PROCEDURE;
                Failsafe.get(schema.getChildObjectList(objectType)).reload();

                DBMethod method = (DBMethod) schema.getChildObject(objectType, factoryInput.getObjectName(), false);
                Failsafe.get(method.getChildObjectList(DBObjectType.ARGUMENT)).reload();
                DatabaseFileSystem.getInstance().openEditor(method, true);
                notifyFactoryEvent(new ObjectFactoryEvent(method, ObjectFactoryEvent.EVENT_TYPE_CREATE));
            } catch (SQLException e) {
                MessageUtil.showErrorDialog(project, "Could not instructions " + factoryInput.getObjectType().getName() + ".", e);
                return false;
            }
        }

        return true;
    }

    public void dropObject(DBSchemaObject object) {
        MessageUtil.showQuestionDialog(
                getProject(),
                "Drop object",
                "Are you sure you want to drop the " + object.getQualifiedNameWithType() + "?",
                MessageUtil.OPTIONS_YES_NO, 0,
                ConnectionAction.create("dropping the object", object, 0, action -> {
                    Project project = getProject();
                    DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
                    databaseFileManager.closeFile(object);

                    BackgroundTask.invoke(project,
                            instructions("Dropping " + object.getQualifiedNameWithType()),
                            (task, progress) -> doDropObject(object));
                }));

    }

    private void doDropObject(DBSchemaObject object) {
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        DBNConnection connection = null;
        try {
            DBContentType contentType = object.getContentType();
            connection = connectionHandler.getPoolConnection(false);

            String objectName = object.getQualifiedName();
            String objectTypeName = object.getTypeName();
            DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
            if (contentType == DBContentType.CODE_SPEC_AND_BODY) {
                DBObjectStatusHolder objectStatus = object.getStatus();
                if (objectStatus.is(DBContentType.CODE_BODY, DBObjectStatus.PRESENT)) {
                    ddlInterface.dropObjectBody(objectTypeName, objectName, connection);
                }

                if (objectStatus.is(DBContentType.CODE_SPEC, DBObjectStatus.PRESENT)) {
                    ddlInterface.dropObject(objectTypeName, objectName, connection);
                }

            } else {
                ddlInterface.dropObject(objectTypeName, objectName, connection);
            }

            DBObjectList objectList = (DBObjectList) object.getParent();
            objectList.reload();

            notifyFactoryEvent(new ObjectFactoryEvent(object, ObjectFactoryEvent.EVENT_TYPE_DROP));
        } catch (SQLException e) {
            String message = "Could not drop " + object.getQualifiedNameWithType() + ".";
            Project project = getProject();
            MessageUtil.showErrorDialog(project, message, e);
        } finally {
            connectionHandler.freePoolConnection(connection);
        }
    }


    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseObjectFactoryManager";
    }
}