package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.panel.DBNPanelImpl;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingListener;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;

import static com.dci.intellij.dbn.common.util.Commons.nvl;

public class AutoCommitLabel extends DBNPanelImpl implements Disposable {
    private interface Colors {
        Color DISCONNECTED = new JBColor(new Color(0x454545), new Color(0x808080));
        Color CONNECTED = new JBColor(new Color(0x454545), new Color(0x808080));
    }
    private ConnectionHandlerRef connectionHandler;
    private WeakRef<VirtualFile> virtualFile;
    private SessionId sessionId;
    private boolean subscribed = false;

    private final JLabel connectionLabel;
    private final JLabel autoCommitLabel;

    public AutoCommitLabel() {
        setLayout(new BorderLayout());
        connectionLabel = new JLabel();
        //connectionLabel.setFont(GUIUtil.BOLD_FONT);
        add(connectionLabel, BorderLayout.EAST);

        autoCommitLabel = new JLabel();
        autoCommitLabel.setFont(Fonts.BOLD);
        add(autoCommitLabel, BorderLayout.WEST);

        add(new JLabel(" "), BorderLayout.CENTER);

    }

    public void init(Project project, VirtualFile virtualFile, ConnectionHandler connectionHandler, DatabaseSession session) {
        init(project, virtualFile, connectionHandler, session == null ? null : session.getId());
    }

    public void init(Project project, VirtualFile virtualFile, ConnectionHandler connectionHandler, SessionId sessionId) {
        this.virtualFile = WeakRef.of(virtualFile);
        this.connectionHandler = ConnectionHandlerRef.of(connectionHandler);
        this.sessionId = nvl(sessionId, SessionId.MAIN);
        if (!subscribed) {
            subscribed = true;
            ProjectEvents.subscribe(project, this, ConnectionStatusListener.TOPIC, connectionStatusListener);
            ProjectEvents.subscribe(project, this, FileConnectionMappingListener.TOPIC, connectionMappingListener);
            ProjectEvents.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
        }
        update();
    }

    private void update() {
        Dispatch.runConditional(() -> {
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (connectionHandler != null && !connectionHandler.isVirtual()) {
                setVisible(true);
                boolean disconnected = !connectionHandler.isConnected(sessionId);
                boolean autoCommit = connectionHandler.isAutoCommit();

                connectionLabel.setForeground(disconnected ? Colors.DISCONNECTED : Colors.CONNECTED);
                DatabaseSession session = connectionHandler.getSessionBundle().getSession(sessionId);


                String sessionName = session.getName();
                connectionLabel.setText(disconnected ? " - not connected" : " - connected");
                connectionLabel.setToolTipText(
                        disconnected ?
                                "Not connected to " + sessionName + " database session" : "");

                connectionLabel.setFont(disconnected ? Fonts.REGULAR : Fonts.BOLD);

                autoCommitLabel.setForeground(autoCommit ?
                        com.dci.intellij.dbn.common.color.Colors.FAILURE_COLOR :
                        com.dci.intellij.dbn.common.color.Colors.SUCCESS_COLOR);
                autoCommitLabel.setText(autoCommit ? "Auto-Commit ON" : "Auto-Commit OFF");
                autoCommitLabel.setToolTipText(
                        autoCommit ?
                                "Auto-Commit is enabled for connection \"" + connectionHandler + "\". Data changes will be automatically committed to the database." :
                                "Auto-Commit is disabled for connection \"" + connectionHandler + "\". Data changes will need to be manually committed to the database.");
            } else {
                setVisible(false);
            }
        });
    }

    @Nullable
    private ConnectionHandler getConnectionHandler() {
        try {
            return ConnectionHandlerRef.get(connectionHandler);
        } catch (AlreadyDisposedException e) {
            this.connectionHandler = null;
            return null;
        }
    }

    private final ConnectionStatusListener connectionStatusListener = (connectionId, sessionId) -> {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null && connectionHandler.getConnectionId() == connectionId) {
            update();
        }
    };

    private final FileConnectionMappingListener connectionMappingListener = new FileConnectionMappingListener() {
        @Override
        public void connectionChanged(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
            VirtualFile localVirtualFile = getVirtualFile();
            if (virtualFile.equals(localVirtualFile)) {
                AutoCommitLabel.this.connectionHandler = ConnectionHandlerRef.of(connectionHandler);
                update();
            }
        }

        @Override
        public void sessionChanged(VirtualFile virtualFile, DatabaseSession session) {
            VirtualFile localVirtualFile = getVirtualFile();
            if (virtualFile.equals(localVirtualFile)) {
                sessionId = session == null ? SessionId.MAIN : session.getId();
                update();
            }
        }
    };

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    private final TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void afterAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            if (action.isOneOf(
                    TransactionAction.TURN_AUTO_COMMIT_ON,
                    TransactionAction.TURN_AUTO_COMMIT_OFF) &&
                    ConnectionHandlerRef.get(AutoCommitLabel.this.connectionHandler) == connectionHandler) {

                update();
            }
        }
    };


    @Nullable
    public VirtualFile getVirtualFile() {
        return WeakRef.get(virtualFile);
    }

    @Override
    protected void disposeInner() {

    }
}
