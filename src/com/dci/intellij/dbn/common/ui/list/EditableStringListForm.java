package com.dci.intellij.dbn.common.ui.list;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.components.JBScrollPane;

public class EditableStringListForm extends DBNFormImpl{
    private JPanel component;
    private JBScrollPane listScrollPane;
    private JPanel actionsPanel;
    private JLabel titleLabel;

    private EditableStringList editableStringList;

    public EditableStringListForm(String title) {
        this(title, new ArrayList<String>());
    }

    public EditableStringListForm(String title, List<String> elements) {
        editableStringList = new EditableStringList(null, elements);
        listScrollPane.setViewportView(editableStringList);
        listScrollPane.getViewport().setBackground(editableStringList.getBackground());
        titleLabel.setText(title);


        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "", false,
                new AddRowAction(),
                new DeleteRowAction());
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    public List<String> getStringValues() {
        return editableStringList.getStringValues();
    }

    public void setStringValues(Collection<String> stringValues) {
        editableStringList.setStringValues(stringValues);
    }

    public class AddRowAction extends AnAction {
        public AddRowAction() {
            super("Add row", null, Icons.ACTION_ADD);
        }

        public void actionPerformed(AnActionEvent anActionEvent) {
            editableStringList.insertRow();
        }
    }

    public class DeleteRowAction extends AnAction {

        public DeleteRowAction() {
            super("Delete row", null, Icons.ACTION_REMOVE);
        }

        public void actionPerformed(AnActionEvent e) {
            editableStringList.removeRow();
        }

        public void update(AnActionEvent e) {
            int selectedRow = editableStringList.getSelectedRow();
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(selectedRow > -1);
        }
    }
}
