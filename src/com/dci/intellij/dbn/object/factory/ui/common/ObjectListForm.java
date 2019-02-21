package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ObjectListForm<T extends ObjectFactoryInput> extends DBNFormImpl<DisposableProjectComponent> {
    private JPanel mainPanel;
    private JPanel listPanel;
    private JPanel actionsPanel;
    private JLabel newLabel;
    private ConnectionHandler connectionHandler;

    private List<ObjectFactoryInputForm<T>> inputForms = new ArrayList<>();

    public ObjectListForm(DisposableProjectComponent parentComponent, ConnectionHandler connectionHandler) {
        super(parentComponent);
        this.connectionHandler = connectionHandler;
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ObjectFactory.AddElement", true,
                new CreateObjectAction());
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        newLabel.setText("Add " + getObjectType().getName());
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    protected abstract ObjectFactoryInputForm<T> createObjectDetailsPanel(int index);
    public abstract DBObjectType getObjectType();

    public void createObjectPanel() {
        ObjectFactoryInputForm<T> inputForm = createObjectDetailsPanel(inputForms.size());
        inputForms.add(inputForm);
        ObjectListItemForm listItemForm = new ObjectListItemForm(this, inputForm);
        listPanel.add(listItemForm.getComponent());

        GUIUtil.repaint(mainPanel);
        inputForm.focus();
    }

    public void removeObjectPanel(ObjectListItemForm child) {
        inputForms.remove(child.getObjectDetailsPanel());
        listPanel.remove(child.getComponent());

        GUIUtil.repaint(mainPanel);
        // rebuild indexes
        for (int i=0; i< inputForms.size(); i++) {
            inputForms.get(i).setIndex(i);
        }
    }

    public List<T> createFactoryInputs(ObjectFactoryInput parent) {
        List<T> objectFactoryInputs = new ArrayList<>();
        for (ObjectFactoryInputForm<T> inputForm : this.inputForms) {
            T objectFactoryInput = inputForm.createFactoryInput(parent);
            objectFactoryInputs.add(objectFactoryInput);
        }
        return objectFactoryInputs;
    }

    public class CreateObjectAction extends AnAction {
        CreateObjectAction() {
            super("Add " + getObjectType().getName(), null, Icons.DATASET_FILTER_CONDITION_NEW);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            createObjectPanel();
        }
    }
}
