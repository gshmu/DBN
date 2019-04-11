package com.dci.intellij.dbn.common.ui.list;

import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EditableStringListForm extends DBNFormImpl<DisposableProjectComponent>{
    private JPanel component;
    private JLabel titleLabel;
    private JPanel listPanel;

    private EditableStringList editableStringList;

    public EditableStringListForm(DisposableProjectComponent parentComponent, String title, boolean sorted) {
        this(parentComponent, title, new ArrayList<String>(), sorted);
    }

    public EditableStringListForm(DisposableProjectComponent parentComponent, String title, List<String> elements, boolean sorted) {
        super(parentComponent);
        editableStringList = new EditableStringList(null, elements, sorted, false);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(editableStringList);
        decorator.setAddAction(anActionButton -> editableStringList.insertRow());
        decorator.setRemoveAction(anActionButton -> editableStringList.removeRow());
        decorator.setMoveUpAction(anActionButton -> editableStringList.moveRowUp());
        decorator.setMoveDownAction(anActionButton -> editableStringList.moveRowDown());
        titleLabel.setText(title);
        //decorator.setPreferredSize(new Dimension(200, 300));
        JPanel editableListPanel = decorator.createPanel();
        Container parent = editableStringList.getParent();
        parent.setBackground(editableStringList.getBackground());
        this.listPanel.add(editableListPanel, BorderLayout.CENTER);
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return component;
    }

    public List<String> getStringValues() {
        return editableStringList.getStringValues();
    }

    public void setStringValues(Collection<String> stringValues) {
        editableStringList.setStringValues(stringValues);
    }

    @Override
    public void disposeInner() {
        Disposer.dispose(editableStringList);
        super.disposeInner();
    }
}
