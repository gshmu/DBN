package com.dci.intellij.dbn.language.editor.action;

import static com.dci.intellij.dbn.common.util.ActionUtil.getProject;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

import java.util.List;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionBundle;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SessionSelectComboBoxAction extends DBNComboBoxAction implements DumbAware {
    private static final String NAME = "Session";

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        Project project = getProject(component);
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        VirtualFile virtualFile = getVirtualFile(component);
        if (virtualFile != null) {
            ConnectionHandler connectionHandler = FileConnectionMappingManager.getInstance(project).getConnectionHandler(virtualFile);
            if (connectionHandler != null && !connectionHandler.isVirtual() && !connectionHandler.isDisposed()) {
                DatabaseSessionBundle sessionBundle = connectionHandler.getSessionBundle();

                if (isDebugConsole(virtualFile)) {
                    actionGroup.add(new SessionSelectAction(sessionBundle.getDebugSession()));
                } else {
                    actionGroup.add(new SessionSelectAction(sessionBundle.getMainSession()));
                    actionGroup.add(new SessionSelectAction(sessionBundle.getPoolSession()));
                    List<DatabaseSession> sessions = sessionBundle.getSessions(ConnectionType.SESSION);
                    if (sessions.size() > 0) {
                        //actionGroup.addSeparator();
                        for (DatabaseSession session : sessions){
                            actionGroup.add(new SessionSelectAction(session));
                        }
                    }
                    actionGroup.addSeparator();
                    actionGroup.add(new SessionCreateAction(connectionHandler));
                    actionGroup.add(new SessionDisableAction(connectionHandler));
                }
            }
        }
        return actionGroup;
    }

    private boolean isDebugConsole(VirtualFile virtualFile) {
        boolean isDebugConsole = false;
        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
            if (consoleVirtualFile.getType() == DBConsoleType.DEBUG) {
                isDebugConsole = true;
            }
        }
        return isDebugConsole;
    }

    public void update(AnActionEvent e) {
        Project project = getProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        String text = NAME;

        Icon icon = null;
        boolean visible = false;
        boolean enabled = true;

        if (project != null && virtualFile != null) {
            FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler connectionHandler = mappingManager.getConnectionHandler(virtualFile);
            visible = connectionHandler != null && !connectionHandler.isVirtual() && connectionHandler.getSettings().getDetailSettings().isEnableSessionManagement();
            if (visible) {
                if (isDebugConsole(virtualFile)) {
                    DatabaseSession debugSession = connectionHandler.getSessionBundle().getDebugSession();
                    text = debugSession.getName();
                    icon = debugSession.getIcon();
                    enabled = false;
                } else {
                    DatabaseSession session = mappingManager.getDatabaseSession(virtualFile);
                    if (session != null) {
                        text = session.getName();
                        icon = session.getIcon();
                        enabled = true;
/*
                    // TODO allow selecting "hot" session?
                    DatabaseSession databaseSession = mappingManager.getDatabaseSession(virtualFile);
                    if (databaseSession != null) {
                        DBNConnection connection = connectionHandler.getConnectionPool().getSessionConnection(databaseSession.getId());
                        enabled = connection == null || !connection.hasDataChanges();

                    } else {
                        enabled = true;
                    }
*/

                    }
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

    @Override
    protected boolean shouldShowDisabledActions() {
        return true;
    }
}
