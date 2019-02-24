package com.dci.intellij.dbn.code.common.intention;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class DatabaseConnectIntentionAction extends GenericIntentionAction implements LowPriorityAction{
    @Override
    @NotNull
    public String getText() {
        return "Connect to database";
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return IntentionActionGroups.CONNECTION;
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.CONNECTION_CONNECTED;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            ConnectionHandler activeConnection = dbLanguagePsiFile.getConnectionHandler();
            if (activeConnection != null && !activeConnection.isDisposed() && !activeConnection.isVirtual() && !activeConnection.canConnect() && !activeConnection.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            ConnectionHandler activeConnection = dbLanguagePsiFile.getConnectionHandler();
            if (activeConnection != null && !activeConnection.isDisposed() && !activeConnection.isVirtual()) {
                activeConnection.getInstructions().setAllowAutoConnect(true);

                DatabaseSession databaseSession = dbLanguagePsiFile.getDatabaseSession();
                SessionId sessionId = databaseSession == null ? SessionId.MAIN : databaseSession.getId();
                SchemaId schemaId = dbLanguagePsiFile.getSchemaId();

                ConnectionAction.invoke(
                        "connecting to database",
                        instructions("Trying to connect to " + activeConnection.getName(), TaskInstruction.MANAGED),
                        activeConnection,
                        action ->  ConnectionManager.testConnection(activeConnection, schemaId, sessionId ,false, true));
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
