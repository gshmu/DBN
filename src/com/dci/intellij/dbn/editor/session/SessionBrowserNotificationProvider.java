package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.session.ui.SessionBrowserErrorNotificationPanel;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SessionBrowserNotificationProvider extends EditorNotifications.Provider<SessionBrowserErrorNotificationPanel> {
    private static final Key<SessionBrowserErrorNotificationPanel> KEY = Key.create("DBNavigator.SessionBrowserErrorNotificationPanel");
    private ProjectRef projectRef;

    public SessionBrowserNotificationProvider(final Project project, @NotNull FrameStateManager frameStateManager) {
        this.projectRef = ProjectRef.from(project);
        EventUtil.subscribe(project, project, SessionBrowserLoadListener.TOPIC, sessionsLoadListener);

    }

    SessionBrowserLoadListener sessionsLoadListener = new SessionBrowserLoadListener() {
        @Override
        public void sessionsLoaded(VirtualFile virtualFile) {
            Project project = getProject();
            if (virtualFile != null && !project.isDisposed()) {
                EditorNotifications notifications = EditorNotifications.getInstance(project);
                notifications.updateNotifications(virtualFile);
            }
        }
    };

    @NotNull
    @Override
    public Key<SessionBrowserErrorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public SessionBrowserErrorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
        if (virtualFile instanceof DBSessionBrowserVirtualFile) {
            if (fileEditor instanceof SessionBrowser) {
                SessionBrowser sessionBrowser = (SessionBrowser) fileEditor;
                ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
                String sourceLoadError = sessionBrowser.getModelError();
                if (StringUtil.isNotEmpty(sourceLoadError)) {
                    return createPanel(connectionHandler, sourceLoadError);
                }

            }
        }
        return null;
    }

    private static SessionBrowserErrorNotificationPanel createPanel(ConnectionHandler connectionHandler, String sourceLoadError) {
        return new SessionBrowserErrorNotificationPanel(connectionHandler, sourceLoadError);
    }

    @NotNull
    public Project getProject() {
        return projectRef.ensure();
    }
}
