package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatus;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.VirtualConnectionHandler;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AutoCommitLabel extends JLabel implements Disposable {
    private interface Colors {
        Color DISCONNECTED = new JBColor(new Color(0x454545), new Color(0x808080));
        Color AUTO_COMMIT_ON = new JBColor(new Color(0xFF0000), new Color(0xBC3F3C));
        Color AUTO_COMMIT_OFF = new JBColor(new Color(0x009600), new Color(0x629755));
    }
    private ConnectionHandlerRef connectionHandlerRef;
    private boolean subscribed = false;

    public AutoCommitLabel() {
        super("");
        setFont(GUIUtil.BOLD_FONT);
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        if (connectionHandler == null || connectionHandler instanceof VirtualConnectionHandler) {
            this.connectionHandlerRef = null;
        } else {
            this.connectionHandlerRef = connectionHandler.getRef();
            if (!subscribed) {
                subscribed = true;
                Project project = connectionHandler.getProject();
                EventUtil.subscribe(project, this, ConnectionHandlerStatusListener.TOPIC, connectionStatusListener);
            }
        }
        update();
    }

    private void update() {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler != null) {
                    setVisible(true);
                    boolean disconnected = !connectionHandler.isConnected();
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
            }
        }.start();
    }

    @Nullable
    private ConnectionHandler getConnectionHandler() {
        try {
            return this.connectionHandlerRef == null ? null : this.connectionHandlerRef.get();
        } catch (AlreadyDisposedException e) {
            this.connectionHandlerRef = null;
            return null;
        }
    }

    private ConnectionHandlerStatusListener connectionStatusListener = new ConnectionHandlerStatusListener() {
        @Override
        public void statusChanged(ConnectionId connectionId, ConnectionHandlerStatus status) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (connectionHandler != null && connectionHandler.getId() == connectionId) {
                update();
            }
        }

    };
    @Override
    public void dispose() {
        connectionHandlerRef = null;
    }


}
