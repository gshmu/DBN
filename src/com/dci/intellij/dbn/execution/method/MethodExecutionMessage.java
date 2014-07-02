package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.vfs.DatabaseContentFile;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;

public class MethodExecutionMessage extends ConsoleMessage {
    private MethodExecutionProcessor executionProcessor;
    private DatabaseEditableObjectFile databaseFile;
    private DatabaseContentFile contentFile;
    private DBContentType contentType;

    public MethodExecutionMessage(MethodExecutionProcessor executionProcessor, String message, MessageType messageType) {
        super(messageType, message);
        this.executionProcessor = executionProcessor;
    }

    public MethodExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }


    public DatabaseEditableObjectFile getDatabaseFile() {
        if (databaseFile == null) {
            databaseFile = executionProcessor.getMethod().getVirtualFile();
        }
        return databaseFile;
    }

    public DatabaseContentFile getContentFile() {
        if (contentFile == null) {
            DatabaseEditableObjectFile databaseFile = getDatabaseFile();
            contentFile = databaseFile.getContentFile(contentType);
        }
        return contentFile;
    }

    public void dispose() {
        executionProcessor = null;
        databaseFile = null;
        contentFile = null;
    }
}
