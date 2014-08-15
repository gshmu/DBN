package com.dci.intellij.dbn.connection.ui;

import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.object.DBSchema;

public class SelectCurrentSchemaDialog extends DBNDialog {
    private DBLanguagePsiFile file;
    private SelectCurrentSchemaForm selectCurrentSchemaForm;

    public SelectCurrentSchemaDialog(DBLanguagePsiFile file) {
        super(file.getProject(), "Select Current Schema", true);
        this.file = file;
        ConnectionHandler activeConnection = file.getActiveConnection();
        selectCurrentSchemaForm = new SelectCurrentSchemaForm(file, activeConnection);
        selectCurrentSchemaForm.setHintMessage(null);
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.SelectSchema";
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return selectCurrentSchemaForm.getComponent();
    }

    public JComponent getPreferredFocusedComponent() {
        return selectCurrentSchemaForm.getSchemasList();
    }

    protected void doOKAction() {
        DBSchema currentSchema = selectCurrentSchemaForm.getSelectedSchema();
        file.setCurrentSchema(currentSchema);
        super.doOKAction();
    }

}
