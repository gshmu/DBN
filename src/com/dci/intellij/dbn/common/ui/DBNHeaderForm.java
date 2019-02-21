package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class DBNHeaderForm extends DBNFormImpl{
    public static final LineBorder BORDER = new LineBorder(UIUtil.getBoundsColor());
    private JLabel objectLabel;
    private JPanel mainPanel;

    public DBNHeaderForm(Disposable parentDisposable) {
        mainPanel.setBorder(BORDER);
        DisposerUtil.register(parentDisposable, this);
    }

    public DBNHeaderForm(String title, Icon icon, Disposable parentDisposable) {
        this(title, icon, null, parentDisposable);
    }

    public DBNHeaderForm(String title, Icon icon, Color background, Disposable parentDisposable) {
        this(parentDisposable);
        objectLabel.setText(title);
        objectLabel.setIcon(icon);
        if (background != null) {
            mainPanel.setBackground(background);
        }
    }

    public DBNHeaderForm(@NotNull DBObject object, Disposable parentDisposable) {
        this(parentDisposable);
        update(object);
    }

    public DBNHeaderForm(@NotNull DBObjectRef objectRef, Disposable parentDisposable) {
        this(parentDisposable);
        update(objectRef);
    }

    public DBNHeaderForm(@NotNull Presentable presentable, Disposable parentDisposable) {
        this(parentDisposable);
        objectLabel.setText(presentable.getName());
        objectLabel.setIcon(presentable.getIcon());
        updateBorderAndBackground(presentable);
    }

    public DBNHeaderForm(@NotNull ConnectionHandler connectionHandler, Disposable parentDisposable) {
        this(parentDisposable);
        objectLabel.setText(connectionHandler.getName());
        objectLabel.setIcon(connectionHandler.getIcon());
        updateBorderAndBackground((Presentable) connectionHandler);
        ConnectionId id = connectionHandler.getConnectionId();
        Project project = connectionHandler.getProject();

        EventUtil.subscribe(project, this, ConnectionHandlerStatusListener.TOPIC, (connectionId) -> {
            if (connectionId == id) {
                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                ConnectionHandler connHandler = connectionManager.getConnectionHandler(connectionId);
                if (connHandler != null) {
                    objectLabel.setIcon(connHandler.getIcon());
                }
            }
        });
    }

    public void update(@NotNull DBObject object) {
        ConnectionHandler connectionHandler = object.getConnectionHandler();

        String connectionName = connectionHandler.getName();
        objectLabel.setText("[" + connectionName + "] " + object.getQualifiedName());
        objectLabel.setIcon(object.getIcon());
        updateBorderAndBackground((Presentable) object);
    }

    public void update(@NotNull DBObjectRef objectRef) {
        ConnectionHandler connectionHandler = objectRef.lookupConnectionHandler();

        String connectionName = connectionHandler == null ? "UNKNOWN" : connectionHandler.getName();
        objectLabel.setText("[" + connectionName + "] " + objectRef.getQualifiedName());
        objectLabel.setIcon(objectRef.getObjectType().getIcon());
        updateBorderAndBackground(objectRef);
    }

    private void updateBorderAndBackground(Presentable presentable) {
        if (presentable instanceof ConnectionProvider) {
            ConnectionProvider connectionProvider = (ConnectionProvider) presentable;
            updateBorderAndBackground(connectionProvider);
        }
        mainPanel.setBorder(BORDER);
    }

    private void updateBorderAndBackground(ConnectionProvider connectionProvider) {
        ConnectionHandler connectionHandler = connectionProvider.getConnectionHandler();
        Color background = null;
        if (connectionHandler != null) {
            Project project = connectionHandler.getProject();
            if (getEnvironmentSettings(project).getVisibilitySettings().getDialogHeaders().value()) {
                background = connectionHandler.getEnvironmentType().getColor();
            }
        }
        mainPanel.setBackground(CommonUtil.nvl(background, UIUtil.getPanelBackground()));
    }

    public void setBackground(Color background) {
        mainPanel.setBackground(background);
    }

    public void setTitle(String title) {
        objectLabel.setText(title);
    }

    public void setIcon(Icon icon) {
        objectLabel.setIcon(icon);
    }

    public Color getBackground() {
        return mainPanel.getBackground();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
