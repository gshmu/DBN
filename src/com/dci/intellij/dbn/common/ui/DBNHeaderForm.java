package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

public class DBNHeaderForm extends DBNFormImpl{
    private JLabel objectLabel;
    private JPanel mainPanel;

    public DBNHeaderForm() {
    }

    public DBNHeaderForm(String title, Icon icon) {
        this(title, icon, null);
    }


    public DBNHeaderForm(String title, Icon icon, Color background) {
        objectLabel.setText(title);
        objectLabel.setIcon(icon);
        if (background != null) {
            mainPanel.setBackground(background);
        }
    }

    public DBNHeaderForm(@NotNull DBObject object) {
        update(object);
    }

    public void update(@NotNull DBObject object) {
        Project project = object.getProject();
        ConnectionHandler connectionHandler = object.getConnectionHandler();

        String connectionName = connectionHandler.getName();
        objectLabel.setText("[" + connectionName + "] " + object.getQualifiedName());
        objectLabel.setIcon(object.getIcon());
        Color background = UIUtil.getPanelBackground();
        if (getEnvironmentSettings(project).getVisibilitySettings().getDialogHeaders().value()) {
            background = object.getEnvironmentType().getColor();
        }

        if (background != null) {
            mainPanel.setBackground(background);
        }
    }

    public DBNHeaderForm(@NotNull Presentable presentable) {
        objectLabel.setText(presentable.getName());
        objectLabel.setIcon(presentable.getIcon());
    }

    public DBNHeaderForm(@NotNull ConnectionHandler connectionHandler) {
        objectLabel.setText(connectionHandler.getName());
        objectLabel.setIcon(connectionHandler.getIcon());
        Color background = UIUtil.getPanelBackground();
        if (getEnvironmentSettings(connectionHandler.getProject()).getVisibilitySettings().getDialogHeaders().value()) {
            background = connectionHandler.getEnvironmentType().getColor();
        }
        if (background != null) {
            mainPanel.setBackground(background);
        }
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
}
