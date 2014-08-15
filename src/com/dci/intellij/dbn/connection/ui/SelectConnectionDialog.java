package com.dci.intellij.dbn.connection.ui;

import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.editor.Editor;

public class SelectConnectionDialog extends DBNDialog implements ListSelectionListener{
    private DBLanguagePsiFile file;
    private SelectConnectionForm selectConnectionForm;

    public SelectConnectionDialog(DBLanguagePsiFile file) {
        super(file.getProject(), "Select Connection", true);
        this.file = file;
        selectConnectionForm = new SelectConnectionForm(file);
        selectConnectionForm.addListSelectionListener(this);
        getOKAction().setEnabled(selectConnectionForm.isValidSelection());
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.SelectConnection";
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return selectConnectionForm.getComponent();
    }

    public JComponent getPreferredFocusedComponent() {
        return selectConnectionForm.getConnectionsList();
    }

    protected void doOKAction() {
        ConnectionHandler activeConnection = selectConnectionForm.getSelectedConnection();
        DBSchema currentSchema = selectConnectionForm.getSelectedSchema();
        file.setActiveConnection(activeConnection);
        file.setCurrentSchema(currentSchema);
        Editor editor = EditorUtil.getSelectedEditor(file.getProject());
        DocumentUtil.touchDocument(editor);
        super.doOKAction();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        getOKAction().setEnabled(selectConnectionForm.isValidSelection());    
    }
}
