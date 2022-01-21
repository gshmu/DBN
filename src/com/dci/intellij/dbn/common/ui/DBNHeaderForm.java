package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.util.Commons;
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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class DBNHeaderForm extends DBNFormImpl{
    public static final LineBorder BORDER = new LineBorder(UIUtil.getBoundsColor());
    private JLabel objectLabel;
    private JPanel mainPanel;

    public DBNHeaderForm(DBNForm parent) {
        super(parent);
        mainPanel.setBorder(BORDER);
    }

    public DBNHeaderForm(DBNForm parent, String title, Icon icon) {
        this(parent, title, icon, null);
    }

    public DBNHeaderForm(DBNForm parent, String title, Icon icon, Color background) {
        this(parent);
        objectLabel.setText(title);
        objectLabel.setIcon(icon);
        if (background != null) {
            mainPanel.setBackground(background);
        }
    }

    public DBNHeaderForm(DBNForm parent, @NotNull DBObject object) {
        this(parent);
        update(object);
    }

    public DBNHeaderForm(DBNForm parent, @NotNull DBObjectRef<?> objectRef) {
        this(parent);
        update(objectRef);
    }

    public DBNHeaderForm(DBNForm parent, @NotNull Presentable presentable) {
        this(parent);
        objectLabel.setText(presentable.getName());
        objectLabel.setIcon(presentable.getIcon());
        updateBorderAndBackground(presentable);
    }

    public DBNHeaderForm(DBNForm parent, @NotNull ConnectionHandler connectionHandler) {
        this(parent);
        objectLabel.setText(connectionHandler.getName());
        objectLabel.setIcon(connectionHandler.getIcon());
        updateBorderAndBackground((Presentable) connectionHandler);
        ConnectionId id = connectionHandler.getConnectionId();
        Project project = connectionHandler.getProject();

        ProjectEvents.subscribe(project, this, ConnectionHandlerStatusListener.TOPIC, (connectionId) -> {
            if (connectionId == id) {
                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                ConnectionHandler connHandler = connectionManager.getConnection(connectionId);
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

    public void update(@NotNull DBObjectRef<?> objectRef) {
        ConnectionHandler connectionHandler = objectRef.resolveConnectionHandler();

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
        mainPanel.setBackground(Commons.nvl(background, UIUtil.getPanelBackground()));
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
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
