package com.dci.intellij.dbn.ddl.ui;

import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.form.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.form.DBNHintForm;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SelectDDLFileForm extends DBNFormBase {
    private JPanel mainPanel;
    private JList<VirtualFile> filesList;
    private JPanel headerPanel;
    private JCheckBox doNotPromptCheckBox;
    private JPanel hintPanel;

    SelectDDLFileForm(DBNDialog<?> parent, DBSchemaObject object, List<VirtualFile> virtualFiles, String hint, boolean isFileOpenEvent) {
        super(parent);
        DBNHeaderForm headerForm = new DBNHeaderForm(this, object);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        DBNHintForm hintForm = new DBNHintForm(this, hint, null, true);
        hintPanel.add(hintForm.getComponent(), BorderLayout.CENTER);

        DefaultListModel<VirtualFile> listModel = new DefaultListModel<VirtualFile>();
        for (VirtualFile virtualFile : virtualFiles) {
            listModel.addElement(virtualFile);
        }
        filesList.setModel(listModel);
        filesList.setCellRenderer(new FileListCellRenderer(getProject()));
        filesList.setSelectedIndex(0);

        if (!isFileOpenEvent) mainPanel.remove(doNotPromptCheckBox);
    }

    public List<VirtualFile> getSelection() {
        return filesList.getSelectedValuesList();
    }

    public void selectAll() {
        filesList.setSelectionInterval(0, filesList.getModel().getSize() -1);
    }

    public void selectNone() {
        filesList.clearSelection();
    }

    public boolean isDoNotPromptSelected() {
        return doNotPromptCheckBox.isSelected();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
