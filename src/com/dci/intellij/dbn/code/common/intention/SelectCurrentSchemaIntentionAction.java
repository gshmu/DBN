package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.SQLConsoleFile;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.List;

public class SelectCurrentSchemaIntentionAction extends GenericIntentionAction {
    @NotNull
    public String getText() {
        return "Select current schema...";
    }

    @NotNull
    public String getFamilyName() {
        return "DBNavigator environment intentions";
    }

    public Icon getIcon(int flags) {
        return Icons.FILE_SCHEMA_MAPPING;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (psiFile instanceof DBLanguageFile && virtualFile != null && (virtualFile.isInLocalFileSystem() || virtualFile instanceof SQLConsoleFile) ) {
            DBLanguageFile file = (DBLanguageFile) psiFile;
            return file.getActiveConnection() != null;
        }
        return false;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        DBLanguageFile dbLanguageFile = (DBLanguageFile) psiFile;

        ConnectionHandler connectionHandler = dbLanguageFile.getActiveConnection();
        if (connectionHandler != null && !connectionHandler.isVirtual() && !connectionHandler.isDisposed()) {
            List<DBSchema> schemas = connectionHandler.getObjectBundle().getSchemas();
            for (DBSchema schema  :schemas) {
                SelectSchemaAction schemaAction = new SelectSchemaAction(schema, dbLanguageFile);
                actionGroup.add(schemaAction);
            }
        }

        ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                "Select schema",
                actionGroup,
                DataManager.getInstance().getDataContext(editor.getComponent()),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true);

        popupBuilder.showCenteredInCurrentWindow(project);


        /*
        // old dialog approach
        DBLanguageFile file = (DBLanguageFile) psiFile;
        SelectCurrentSchemaDialog selectCurrentSchemaDialog = new SelectCurrentSchemaDialog(file);
        selectCurrentSchemaDialog.show();
        */

    }


    private class SelectSchemaAction extends AnAction {
        private DBSchema schema;
        private DBLanguageFile file;

        private SelectSchemaAction(DBSchema schema, DBLanguageFile file) {
            super(NamingUtil.enhanceUnderscoresForDisplay(schema.getName()), null, schema.getIcon());
            this.file = file;
            this.schema = schema;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            file.setCurrentSchema(schema);

        }
    }

    public boolean startInWriteAction() {
        return false;
    }
}
