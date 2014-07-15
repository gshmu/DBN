package com.dci.intellij.dbn.object.factory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.factory.ui.FunctionFactoryInputForm;
import com.dci.intellij.dbn.object.factory.ui.ProcedureFactoryInputForm;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputDialog;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputForm;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import gnu.trove.THashSet;

public class DatabaseObjectFactory extends AbstractProjectComponent {
    private Set<ObjectFactoryListener> factoryListeners = new THashSet<ObjectFactoryListener>();
    private DatabaseObjectFactory(Project project) {
        super(project);
    }

    public static DatabaseObjectFactory getInstance(Project project) {
        return project.getComponent(DatabaseObjectFactory.class);
    }

    public void addFactoryListener(ObjectFactoryListener factoryListener) {
        factoryListeners.add(factoryListener);
    }

    public void removeFactoryListener(ObjectFactoryListener factoryListener) {
        factoryListeners.remove(factoryListener);
    }

    private void notifyFactoryEvent(ObjectFactoryEvent event) {
        for (ObjectFactoryListener factoryListener : factoryListeners) {
            if (event.getEventType() == ObjectFactoryEvent.EVENT_TYPE_CREATE) {
                factoryListener.objectCreated(event.getObject());
            } else if (event.getEventType() == ObjectFactoryEvent.EVENT_TYPE_DROP) {
                factoryListener.objectDropped(event.getObject());
            }
        }
    }


    public void openFactoryInputDialog(DBSchema schema, DBObjectType objectType) {
        ObjectFactoryInputForm inputForm =
            objectType == DBObjectType.FUNCTION ? new FunctionFactoryInputForm(schema, objectType, 0) :
            objectType == DBObjectType.PROCEDURE ? new ProcedureFactoryInputForm(schema, objectType, 0) : null;

        if (inputForm == null) {
            MessageUtil.showErrorDialog("Creation of " + objectType.getListName() + " is not supported yet.", "Operation not supported");
        } else {
            ObjectFactoryInputDialog dialog = new ObjectFactoryInputDialog(inputForm);
            dialog.show();
        }
    }

    public boolean createObject(ObjectFactoryInput factoryInput) {
        List<String> errors = new ArrayList<String>();
        factoryInput.validate(errors);
        if (errors.size() > 0) {
            StringBuilder buffer = new StringBuilder("Could not create " + factoryInput.getObjectType().getName() + ". Please correct following errors: \n");
            for (String error : errors) {
                buffer.append(" - ").append(error).append("\n");
            }
            MessageUtil.showErrorDialog(buffer.toString());
            return false;
        }
        if (factoryInput instanceof MethodFactoryInput) {
            MethodFactoryInput methodFactoryInput = (MethodFactoryInput) factoryInput;
            DBSchema schema = methodFactoryInput.getSchema();
            try {
                ConnectionHandler connectionHandler = schema.getConnectionHandler();
                Connection connection = connectionHandler.getStandaloneConnection(schema);
                connectionHandler.getInterfaceProvider().getDDLInterface().createMethod(methodFactoryInput, connection);
                DBObjectType objectType = methodFactoryInput.isFunction() ? DBObjectType.FUNCTION : DBObjectType.PROCEDURE;
                schema.getChildObjectList(objectType).reload();
                DBMethod method = (DBMethod) schema.getChildObject(objectType, factoryInput.getObjectName(), false);
                method.getChildObjectList(DBObjectType.ARGUMENT).reload();
                DatabaseFileSystem.getInstance().openEditor(method);
                notifyFactoryEvent(new ObjectFactoryEvent(method, ObjectFactoryEvent.EVENT_TYPE_CREATE));
            } catch (SQLException e) {
                MessageUtil.showErrorDialog("Could not create " + factoryInput.getObjectType().getName() + ".", e);
                return false;
            }
        }

        return true;
    }

    public void dropObject(final DBSchemaObject object) {
        final ConnectionHandler connectionHandler = object.getConnectionHandler();
        final DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
        int response = Messages.showYesNoDialog(
                "Are you sure you want to drop the " + object.getQualifiedNameWithType() + "?",
                Constants.DBN_TITLE_PREFIX + "Drop object",
                Messages.getQuestionIcon());
        if (response == 0) {
            new BackgroundTask(object.getProject(), "Dropping " + object.getQualifiedNameWithType(), false) {
                public void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    try {
                        initProgressIndicator(progressIndicator, true);
                        ddlInterface.dropObject(object.getTypeName(), object.getQualifiedName(), connectionHandler.getStandaloneConnection());

                        /*Messages.showInfoMessage(
                        NamingUtil.capitalize(object.getTypeName()) + " " + object.getQualifiedName() + " was dropped successfully.",
                        "Database Navigator - Object dropped");*/

                        DBObjectList objectList = (DBObjectList) object.getTreeParent();
                        objectList.reload();
                        notifyFactoryEvent(new ObjectFactoryEvent(object, ObjectFactoryEvent.EVENT_TYPE_DROP));
                    } catch (SQLException e) {
                        String message = "Could not drop " + object.getQualifiedNameWithType() + ".";
                        MessageUtil.showErrorDialog(message, e);
                    }

                }
            }.start();
        }
    }


    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseObjectFactoryManager";
    }
}