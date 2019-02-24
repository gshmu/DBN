package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.EventUtil;
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

import javax.swing.*;
import java.awt.*;

import static com.dci.intellij.dbn.common.util.CommonUtil.nvl;

public class AutoCommitLabel extends JLabel implements Disposable {
    private interface Colors {
        Color DISCONNECTED = new JBColor(new Color(0x454545), new Color(0x808080));
        Color AUTO_COMMIT_ON = new JBColor(new Color(0xFF0000), new Color(0xBC3F3C));
        Color AUTO_COMMIT_OFF = new JBColor(new Color(0x009600), new Color(0x629755));
    }
    private ConnectionHandlerRef connectionHandlerRef;
    private SessionId sessionId;
    private boolean subscribed = false;
    private WeakRef<VirtualFile> virtualFileRef;

    public AutoCommitLabel() {
        super("");
        setFont(GUIUtil.BOLD_FONT);
    }

    public void init(Project project, VirtualFile virtualFile, ConnectionHandler connectionHandler, DatabaseSession session) {
        init(project, virtualFile, connectionHandler, session == null ? null : session.getId());
    }

    public void init(Project project, VirtualFile virtualFile, ConnectionHandler connectionHandler, SessionId sessionId) {
        this.virtualFileRef = WeakRef.from(virtualFile);
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
        this.sessionId = nvl(sessionId, SessionId.MAIN);
        if (!subscribed) {
            subscribed = true;
            EventUtil.subscribe(project, this, ConnectionStatusListener.TOPIC, connectionStatusListener);
            EventUtil.subscribe(project, this, FileConnectionMappingListener.TOPIC, connectionMappingListener);
            EventUtil.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
        }
        update();
    }

    private void update() {
        Dispatch.invoke(() -> {
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (connectionHandler != null) {
                setVisible(true);
                boolean disconnected = !connectionHandler.isConnected(sessionId);
                boolean autoCommit = connectionHandler.isAutoCommit();
                setText(disconnected ? "Not connected to database" : autoCommit ? "Auto-Commit ON" : "Auto-Commit OFF");
                setForeground(disconnected ?
                        Colors.DISCONNECTED : autoCommit ?
                        Colors.AUTO_COMMIT_ON :
                        Colors.AUTO_COMMIT_OFF);
                setToolTipText(
                        disconnected ? "The connection to database has been closed. No editing possible" :
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
            return this.connectionHandlerRef == null ? null : this.connectionHandlerRef.getnn();
        } catch (AlreadyDisposedException e) {
            this.connectionHandlerRef = null;
            return null;
        }
    }

    private ConnectionStatusListener connectionStatusListener = (connectionId, sessionId) -> {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null && connectionHandler.getConnectionId() == connectionId) {
            update();
        }
    };

    private FileConnectionMappingListener connectionMappingListener = new FileConnectionMappingListener() {
        @Override
        public void connectionChanged(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
            VirtualFile localVirtualFile = getVirtualFile();
            if (virtualFile.equals(localVirtualFile)) {
                AutoCommitLabel.this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
                update();
            }
        }

        @Override
        public void sessionChanged(VirtualFile virtualFile, DatabaseSession session) {
            VirtualFile localVirtualFile = getVirtualFile();
            if (virtualFile.equals(localVirtualFile)) {
                AutoCommitLabel.this.sessionId = session == null ? SessionId.MAIN : session.getId();
                update();
            }
        }
    };

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    private TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void afterAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            if (action.isOneOf(TransactionAction.TURN_AUTO_COMMIT_ON, TransactionAction.TURN_AUTO_COMMIT_OFF) && ConnectionHandlerRef.get(connectionHandlerRef) == connectionHandler) {
                update();
            }
        }
    };


    @Nullable
    public VirtualFile getVirtualFile() {
        return WeakRef.get(virtualFileRef);
    }

    @Override
    public void dispose() {
        connectionHandlerRef = null;
    }


}
