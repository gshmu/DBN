package com.dci.intellij.dbn.navigation.action;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.navigation.GoToDatabaseObjectModel;
import com.dci.intellij.dbn.navigation.options.ObjectsLookupSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.options.GlobalProjectSettings;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;

import java.util.List;

public class GoToDatabaseObjectAction extends GotoActionBase implements DumbAware {
    private ConnectionHandler latestSelection; // todo move to data context
    private String latestPredefinedText;
    public void gotoActionPerformed(AnActionEvent event) {
        //FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project != null) {
            ObjectsLookupSettings objectsLookupSettings = GlobalProjectSettings.getInstance(project).getNavigationSettings().getObjectsLookupSettings();
            if (objectsLookupSettings.getPromptConnectionSelection().value()) {
                ConnectionHandler singleConnectionHandler = null;
                DefaultActionGroup actionGroup = new DefaultActionGroup();

                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                List<ConnectionBundle> connectionBundles = connectionManager.getConnectionBundles();
                for (ConnectionBundle connectionBundle : connectionBundles) {
                    if (connectionBundle.getConnectionHandlers().size() > 0) {
                        if ((actionGroup.getChildrenCount() > 1)) {
                            actionGroup.addSeparator();
                        }

                        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                            SelectConnectionAction connectionAction = new SelectConnectionAction(connectionHandler);
                            actionGroup.add(connectionAction);
                            singleConnectionHandler = connectionHandler;
                        }
                    }
                }

                if (actionGroup.getChildrenCount() > 1) {
                    removeActionLock();
                    ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                            "Select connection for lookup",
                            actionGroup,
                            event.getDataContext(),
                            //JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                            true,
                            true,
                            true,
                            null,
                            actionGroup.getChildrenCount(),
                            new Condition<AnAction>() {
                                public boolean value(AnAction action) {
                                    SelectConnectionAction selectConnectionAction = (SelectConnectionAction) action;
                                    return latestSelection == selectConnectionAction.connectionHandler;
                                }
                            });

                    popupBuilder.showCenteredInCurrentWindow(project);
                } else {
                    showLookupPopup(event, project, singleConnectionHandler, null);
                }
            } else {
                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                ConnectionHandler connectionHandler = connectionManager.getActiveConnection(project);
                showLookupPopup(event, project, connectionHandler, null);
            }
        }
    }


    private class SelectConnectionAction extends AnAction{
        private ConnectionHandler connectionHandler;

        private SelectConnectionAction(ConnectionHandler connectionHandler) {
            super(connectionHandler.getName(), null, connectionHandler.getIcon());
            this.connectionHandler = connectionHandler;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            Project project = connectionHandler.getProject();
            showLookupPopup(e, project, connectionHandler, null);
            latestSelection = connectionHandler;
        }
    }

    private void showLookupPopup(AnActionEvent e, Project project, ConnectionHandler connectionHandler, DBSchema selectedSchema) {
        if (connectionHandler == null) {
            // remove action lock here since the pop-up will not be fired to remove it onClose()
            removeActionLock();
        } else {
            GoToDatabaseObjectModel model = new GoToDatabaseObjectModel(project, connectionHandler, selectedSchema);
            String predefinedText = getPredefinedText(project);

            ChooseByNamePopup popup = ChooseByNamePopup.createPopup(project, model, getPsiContext(e), predefinedText);
            popup.invoke(new Callback(model), ModalityState.current(), false);
        }
    }

    private String getPredefinedText(Project project) {
        String predefinedText = null;
        FileEditor[] selectedEditors = FileEditorManager.getInstance(project).getSelectedEditors();
        for (FileEditor fileEditor : selectedEditors) {
            if (fileEditor instanceof BasicTextEditor) {
                BasicTextEditor textEditor = (BasicTextEditor) fileEditor;
                predefinedText = textEditor.getEditor().getSelectionModel().getSelectedText();
            } else if (fileEditor instanceof TextEditor) {
                TextEditor textEditor = (TextEditor) fileEditor;
                predefinedText = textEditor.getEditor().getSelectionModel().getSelectedText();
            }
            if (isValidPredefinedText(predefinedText)) {
                break;
            } else {
                predefinedText = null;
            }
        }

        if (predefinedText == null) {
            predefinedText = ClipboardUtil.getStringContent();
            if (!isValidPredefinedText(predefinedText)) {
                predefinedText = latestPredefinedText;
            }
        }


        latestPredefinedText = StringUtil.trim(predefinedText);
        return latestPredefinedText;
    }

    private boolean isValidPredefinedText(String predefinedText) {
        return predefinedText != null && !predefinedText.contains("\n") && predefinedText.trim().length() < 50;
    }

    private void removeActionLock() {
        if (GoToDatabaseObjectAction.class.equals(myInAction)) {
            myInAction = null;
        }
    }

    private class Callback extends ChooseByNamePopupComponent.Callback {
        private GoToDatabaseObjectModel model;

        private Callback(GoToDatabaseObjectModel model) {
            this.model = model;
        }

        @Override
        public void elementChosen(Object element) {
            if (element instanceof DBObject) {
                DBObject object = (DBObject) element;
                if (object.getProperties().is(DBObjectProperty.EDITABLE)) {
                    DatabaseFileSystem.getInstance().openEditor(object);
                } else {
                    object.navigate(true);
                }
            }
        }

        @Override
        public void onClose() {
            removeActionLock();
        }
    }

    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setText("Database Object...");
    }
}
