package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DatabaseConnectIntentionAction extends GenericIntentionAction implements LowPriorityAction{
    @Override
    @NotNull
    public String getText() {
        return "Connect to database";
    }


    @Override
    public Icon getIcon(int flags) {
        return Icons.CONNECTION_CONNECTED;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            ConnectionHandler activeConnection = dbLanguagePsiFile.getConnection();
            if (Failsafe.check(activeConnection) && !activeConnection.isVirtual() && !activeConnection.canConnect() && !activeConnection.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            ConnectionHandler connectionHandler = dbLanguagePsiFile.getConnection();
            if (Failsafe.check(connectionHandler) && !connectionHandler.isVirtual()) {
                connectionHandler.getInstructions().setAllowAutoConnect(true);

                DatabaseSession databaseSession = dbLanguagePsiFile.getSession();
                SessionId sessionId = databaseSession == null ? SessionId.MAIN : databaseSession.getId();
                SchemaId schemaId = dbLanguagePsiFile.getSchemaId();

                ConnectionAction.invoke("", true, connectionHandler,
                        (action) -> ConnectionManager.testConnection(connectionHandler, schemaId, sessionId, false, true));
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return super.getGroupPriority();
    }
}
