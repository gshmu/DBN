package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.common.compatibility.LegacyEditorNotificationsProvider;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.session.ui.SessionBrowserErrorNotificationPanel;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SessionBrowserNotificationProvider extends LegacyEditorNotificationsProvider<SessionBrowserErrorNotificationPanel> {
    private static final Key<SessionBrowserErrorNotificationPanel> KEY = Key.create("DBNavigator.SessionBrowserErrorNotificationPanel");

    public SessionBrowserNotificationProvider() {
        this(null);
    }

    public SessionBrowserNotificationProvider(@Nullable Project project) {
        super(project);
        subscribe(SessionBrowserLoadListener.TOPIC, sessionsLoadListener);

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
    public SessionBrowserErrorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor, @NotNull Project project) {
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
}
