package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionBundle;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class SelectDatabaseSessionComboBoxAction extends DBNComboBoxAction implements DumbAware {
    private static final String NAME = "Session";

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        Project project = ActionUtil.getProject(component);
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(DataManager.getInstance().getDataContext(component));
        if (virtualFile != null) {
            ConnectionHandler connectionHandler = FileConnectionMappingManager.getInstance(project).getConnectionHandler(virtualFile);
            if (connectionHandler != null && !connectionHandler.isVirtual() && !connectionHandler.isDisposed()) {
                DatabaseSessionBundle sessionBundle = connectionHandler.getSessionBundle();
                actionGroup.add(new SelectDatabaseSessionAction(sessionBundle.getMainSession()));
                actionGroup.add(new SelectDatabaseSessionAction(sessionBundle.getPoolSession()));
                List<DatabaseSession> sessions = sessionBundle.getSessions();
                if (sessions.size() > 0) {
                    //actionGroup.addSeparator();
                    for (DatabaseSession session : sessions){
                        if (session.isCustom()) {
                            actionGroup.add(new SelectDatabaseSessionAction(session));
                        }
                    }
                }
                actionGroup.addSeparator();
                actionGroup.add(new CreateDatabaseSessionAction(connectionHandler));
            }
        }
        return actionGroup;
    }

    public void update(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        String text = NAME;

        Icon icon = null;
        boolean visible = false;
        boolean enabled = true;

        if (project != null && virtualFile != null) {
            FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler connectionHandler = mappingManager.getConnectionHandler(virtualFile);
            visible = connectionHandler != null && !connectionHandler.isVirtual();
            if (visible) {
                DatabaseSession session = mappingManager.getDatabaseSession(virtualFile);
                if (session != null) {
                    text = session.getName();
                    icon = session.getIcon();
                    enabled = true;
                }
            }
        }

        Presentation presentation = e.getPresentation();
        presentation.setText(text, false);
        presentation.setDescription("Select database session");
        presentation.setIcon(icon);
        presentation.setVisible(visible);
        presentation.setEnabled(enabled);
    }
 }
