package com.dci.intellij.dbn.data.editor.text.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.data.editor.text.TextEditorAdapter;
import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TextEditorDialog extends DBNDialog<TextEditorForm> {
    private final TextEditorAdapter textEditorAdapter;
    private TextEditorDialog(Project project, TextEditorAdapter textEditorAdapter){
        super(project, getTitle(textEditorAdapter), true);
        this.textEditorAdapter = textEditorAdapter;
        getCancelAction().putValue(Action.NAME, "Close");
        getOKAction().setEnabled(false);
        setModal(true);
        init();
    }

    @NotNull
    @Override
    protected TextEditorForm createComponent() {
        UserValueHolder userValueHolder = textEditorAdapter.getUserValueHolder();
        return new TextEditorForm(this, documentListener, userValueHolder, textEditorAdapter);
    }

    @NotNull
    private static String getTitle(TextEditorAdapter textEditorAdapter) {
        UserValueHolder userValueHolder = textEditorAdapter.getUserValueHolder();
        DBDataType dataType = userValueHolder.getDataType();
        String dataTypeName = dataType == null ? "OBJECT" : dataType.getName();
        DBObjectType objectType = userValueHolder.getObjectType();
        return "Edit " + dataTypeName.toUpperCase() + " content (" +objectType.getName().toLowerCase() + " " + userValueHolder.getName().toUpperCase() + ")";
    }

    public static void show(Project project, TextEditorAdapter textEditorAdapter) {
        TextEditorDialog dialog = new TextEditorDialog(project, textEditorAdapter);
        dialog.show();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        try {
            getComponent().writeUserValue();
        } finally {
            super.doOKAction();
        }
    }

    private DocumentListener documentListener = new DocumentAdapter() {
        @Override
        public void documentChanged(DocumentEvent event) {
            getCancelAction().putValue(Action.NAME, "Cancel");
            getOKAction().setEnabled(true);
        }
    };
}
