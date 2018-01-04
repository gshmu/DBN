package com.dci.intellij.dbn.ddl.ui;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class SelectDDLFileForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JList<VirtualFile> filesList;
    private JPanel headerPanel;
    private JCheckBox doNotPromptCheckBox;
    private JPanel hintPanel;

    SelectDDLFileForm(DBSchemaObject object, List<VirtualFile> virtualFiles, String hint, boolean isFileOpenEvent) {
        Project project = object.getProject();
        DBNHeaderForm headerForm = new DBNHeaderForm(object, this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        DBNHintForm hintForm = new DBNHintForm(hint, null, true);
        hintPanel.add(hintForm.getComponent(), BorderLayout.CENTER);

        DefaultListModel<VirtualFile> listModel = new DefaultListModel<VirtualFile>();
        for (VirtualFile virtualFile : virtualFiles) {
            listModel.addElement(virtualFile);
        }
        filesList.setModel(listModel);
        filesList.setCellRenderer(new FileListCellRenderer(project));
        filesList.setSelectedIndex(0);

        if (!isFileOpenEvent) mainPanel.remove(doNotPromptCheckBox);
    }

    public Object[] getSelection() {
        return filesList.getSelectedValues();
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

    public JPanel getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
    }
}
