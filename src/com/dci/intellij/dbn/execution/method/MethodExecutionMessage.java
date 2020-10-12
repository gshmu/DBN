package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class MethodExecutionMessage extends ConsoleMessage {
    private @Getter @Setter MethodExecutionProcessor<?> executionProcessor;
    private DBEditableObjectVirtualFile databaseFile;
    private DBContentVirtualFile contentFile;
    private DBContentType contentType;

    public MethodExecutionMessage(MethodExecutionProcessor<?> executionProcessor, String message, MessageType messageType) {
        super(messageType, message);
        this.executionProcessor = executionProcessor;
    }

    public DBEditableObjectVirtualFile getDatabaseFile() {
        if (databaseFile == null) {
            DBMethod method = executionProcessor.getMethod();
            databaseFile = method.getEditableVirtualFile();
        }
        return databaseFile;
    }

    @Nullable
    public DBContentVirtualFile getContentFile() {
        if (contentFile == null) {
            DBEditableObjectVirtualFile databaseFile = getDatabaseFile();
            contentFile = databaseFile.getContentFile(contentType);
        }
        return contentFile;
    }
}
