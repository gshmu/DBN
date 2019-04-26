package com.dci.intellij.dbn.navigation.action;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.navigation.GoToDatabaseObjectModel;
import com.dci.intellij.dbn.navigation.options.ObjectsLookupSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GoToDatabaseObjectAction extends GotoActionBase implements DumbAware {
    private ConnectionId latestConnectionId;
    private String latestSchemaName = "";
    private String latestUsedText;
    private String latestPredefinedText;
    private String latestClipboardText;
    private ChooseByNamePopup popup;
    @Override
    public void gotoActionPerformed(AnActionEvent event) {

        //FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        Project project = event.getData(PlatformDataKeys.PROJECT);

        if (project != null) {
            ObjectsLookupSettings objectsLookupSettings = ProjectSettingsManager.getSettings(project).getNavigationSettings().getObjectsLookupSettings();
            if (objectsLookupSettings.getPromptConnectionSelection().value()) {
                ConnectionHandler singleConnectionHandler = null;
                DefaultActionGroup actionGroup = new DefaultActionGroup();

                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
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

                if (actionGroup.getChildrenCount() > 1) {
                    removeActionLock();
                    ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                            "Select Connection / Schema for Lookup",
                            actionGroup,
                            event.getDataContext(),
                            //JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                            false,
                            true,
                            true,
                            null,
                            actionGroup.getChildrenCount(),
                            preselect -> {
                                if (preselect instanceof SelectConnectionAction) {
                                    SelectConnectionAction selectConnectionAction = (SelectConnectionAction) preselect;
                                    return latestConnectionId == selectConnectionAction.getConnectionHandler().getConnectionId();
                                } else if (preselect instanceof SelectSchemaAction) {
                                    SelectSchemaAction selectSchemaAction = (SelectSchemaAction) preselect;
                                    DBSchema object = selectSchemaAction.getTarget();
                                    return object != null && latestSchemaName.equals(object.getName());
                                }
                                return false;
                            });

/*                    if (popupBuilder instanceof ListPopupImpl) {
                        ListPopupImpl listPopup = (ListPopupImpl) popupBuilder;
                        listPopup.getList().setCellRenderer(new DefaultListCellRenderer(){
                            @Override
                            public Component getListCellRendererComponent(JList<?> actions, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                PopupFactoryImpl.ActionItem actionItem  = (PopupFactoryImpl.ActionItem) value;
                                Component component = super.getListCellRendererComponent(actions, value, index, isSelected, cellHasFocus);
                                if (component instanceof JLabel) {
                                    JLabel label = (JLabel) component;
                                    label.setIcon(actionItem.getIcon());
                                    label.setText(actionItem.getText().replace("&", ""));
                                    AnAction action = actionItem.getAction();
                                    if (!isSelected && action instanceof SelectConnectionAction) {
                                        SelectConnectionAction selectConnectionAction = (SelectConnectionAction) action;
                                        label.setBackground(selectConnectionAction.connectionHandler.getEnvironmentType().getColor());
                                    }
                                }
                                return component;
                            }
                        });
                    }*/
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


    private class SelectConnectionAction extends ActionGroup {
        private ConnectionHandlerRef connectionHandlerRef;

        private SelectConnectionAction(ConnectionHandler connectionHandler) {
            super();
            connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
            Presentation presentation = getTemplatePresentation();
            presentation.setText(connectionHandler.getName(), false);
            presentation.setIcon(connectionHandler.getIcon());
            setPopup(true);
        }

        public ConnectionHandler getConnectionHandler() {
            return connectionHandlerRef.ensure();
        }

        @Override
        public boolean canBePerformed(DataContext context) {
            return true;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            Project project = connectionHandler.getProject();
            showLookupPopup(e, project, connectionHandler, null);
            latestConnectionId = connectionHandler.getConnectionId();
        }

        @NotNull
        @Override
        public AnAction[] getChildren(AnActionEvent e) {
            List<SelectSchemaAction> schemaActions = new ArrayList<SelectSchemaAction>();
            ConnectionHandler connectionHandler = getConnectionHandler();
            for (DBSchema schema : connectionHandler.getObjectBundle().getSchemas()) {
                schemaActions.add(new SelectSchemaAction(schema));
            }
            return schemaActions.toArray(new AnAction[0]);
        }
    }

    private class SelectSchemaAction extends AnObjectAction<DBSchema> {
        private SelectSchemaAction(DBSchema schema) {
            super(schema);
        }

        @Override
        protected void actionPerformed(
                @NotNull AnActionEvent e,
                @NotNull Project project,
                @NotNull DBSchema object) {

            showLookupPopup(e, project, object.getConnectionHandler(), object);
            latestSchemaName = object.getName();
        }
    }


    private void showLookupPopup(AnActionEvent e, Project project, ConnectionHandler connectionHandler, DBSchema selectedSchema) {
        if (connectionHandler == null) {
            // remove action lock here since the pop-up will not be fired to remove it onClose()
            removeActionLock();
        } else {
            GoToDatabaseObjectModel model = new GoToDatabaseObjectModel(project, connectionHandler, selectedSchema);
            String predefinedText = getPredefinedText(project);

            popup = ChooseByNamePopup.createPopup(project, model, getPsiContext(e), predefinedText);
            popup.invoke(new Callback(model), ModalityState.current(), false);
        }
    }

    private String getPredefinedText(Project project) {
        String predefinedText = null;
        FileEditor[] selectedEditors = FileEditorManager.getInstance(project).getSelectedEditors();
        for (FileEditor fileEditor : selectedEditors) {
            Editor editor = EditorUtil.getEditor(fileEditor);
            if (editor != null) {
                predefinedText = editor.getSelectionModel().getSelectedText();
            }
            if (isValidPredefinedText(predefinedText)) {
                break;
            } else {
                predefinedText = null;
            }
        }

        String clipboardText = StringUtil.trim(ClipboardUtil.getStringContent());
        if (predefinedText == null) {
            if (isValidPredefinedText(clipboardText)) {
                if (StringUtil.isNotEmpty(latestUsedText) &&
                        clipboardText.equals(latestClipboardText) &&
                        !latestUsedText.equals(clipboardText)) {

                    predefinedText = latestUsedText;
                } else {
                    predefinedText = clipboardText;
                }
            } else {
                predefinedText = latestPredefinedText;

            }
        }

        latestClipboardText = clipboardText;
        latestPredefinedText = StringUtil.trim(predefinedText);
        return latestPredefinedText;
    }

    private static boolean isValidPredefinedText(String predefinedText) {
        return predefinedText != null && !predefinedText.contains("\n") && predefinedText.trim().length() < 50;
    }

    private static void removeActionLock() {
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
                if (object.is(DBObjectProperty.EDITABLE)) {
                    DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
                    databaseFileSystem.connectAndOpenEditor(object, null, false, true);
                } else {
                    object.navigate(true);
                }
            }
        }

        @Override
        public void onClose() {
            removeActionLock();
            Disposer.dispose(model);
            latestUsedText = popup.getEnteredText();
            popup = null;
        }
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setText("Database Object...");
    }
}
