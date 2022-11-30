package com.dci.intellij.dbn.execution.explain.result;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class ExplainPlanMessage extends ConsoleMessage {

    @Getter
    private final ExplainPlanResult explainPlanResult;

    public ExplainPlanMessage(ExplainPlanResult explainPlanResult, MessageType messageType) {
        super(messageType, explainPlanResult.getErrorMessage());
        this.explainPlanResult = explainPlanResult;

        Disposer.register(this, explainPlanResult);
    }

    @Nullable
    @Override
    public ConnectionId getConnectionId() {
        return explainPlanResult.getConnectionId();
    }

    public VirtualFile getVirtualFile() {
        return explainPlanResult.getVirtualFile();
    }

    @Deprecated
    public void navigateToEditor(boolean requestFocus) {
        //executionResult.getExecutionProcessor().navigateToEditor(requestFocus);
    }

    public ConnectionHandler getConnection() {
        return explainPlanResult.getConnection();
    }
}
