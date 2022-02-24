package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableContainers;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public abstract class ObjectListForm<T extends ObjectFactoryInput> extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel listPanel;
    private JPanel actionsPanel;
    private JLabel newLabel;
    private final ConnectionHandlerRef connection;

    private final List<ObjectFactoryInputForm<T>> inputForms = DisposableContainers.list(this);

    public ObjectListForm(DBNComponent parent, @NotNull ConnectionHandler connection) {
        super(parent);
        this.connection = connection.ref();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,
                "DBNavigator.ObjectFactory.AddElement", true,
                new CreateObjectAction());
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        newLabel.setText("Add " + getObjectType().getName());
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public ConnectionHandler getConnection() {
        return connection.ensure();
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
