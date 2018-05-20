package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatus;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
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
        updateBorderAndBackground(connectionHandler);
        final ConnectionId id = connectionHandler.getId();
        final Project project = connectionHandler.getProject();

        EventUtil.subscribe(project, this, ConnectionHandlerStatusListener.TOPIC, new ConnectionHandlerStatusListener() {
            @Override
            public void statusChanged(ConnectionId connectionId, ConnectionHandlerStatus status) {
                if (connectionId == id) {
                    ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                    ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);
                    if (connectionHandler != null) {
                        objectLabel.setIcon(connectionHandler.getIcon());
                    }
                }
            }
        });
    }

    public void update(@NotNull DBObject object) {
        ConnectionHandler connectionHandler = object.getConnectionHandler();

        String connectionName = connectionHandler.getName();
        objectLabel.setText("[" + connectionName + "] " + object.getQualifiedName());
        objectLabel.setIcon(object.getIcon());
        updateBorderAndBackground(object);
    }

    private void updateBorderAndBackground(Presentable presentable) {
        if (presentable instanceof ConnectionProvider) {
            ConnectionProvider connectionProvider = (ConnectionProvider) presentable;
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
        mainPanel.setBorder(BORDER);
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

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
