package com.dci.intellij.dbn.execution.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.openapi.vfs.VirtualFile;

public class LogOutputContext {
    private ConnectionHandlerRef connectionHandlerRef;
    private VirtualFile sourceFile;
    private Process process;
    private boolean hideEmptyLines = false;
    private boolean cancelled = false;

    public LogOutputContext(@NotNull ConnectionHandler connectionHandler) {
        this(connectionHandler, null, null);
    }

    public LogOutputContext(@NotNull ConnectionHandler connectionHandler, @Nullable VirtualFile sourceFile, @Nullable Process process) {
        this.connectionHandlerRef = connectionHandler.getRef();
        this.sourceFile = sourceFile;
        this.process = process;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Nullable
    public VirtualFile getSourceFile() {
        return sourceFile;
    }

    @Nullable
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public boolean isHideEmptyLines() {
        return hideEmptyLines;
    }

    public void setHideEmptyLines(boolean hideEmptyLines) {
        this.hideEmptyLines = hideEmptyLines;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean matches(LogOutputContext context) {
        return getConnectionHandler() == context.getConnectionHandler() &&
                CommonUtil.safeEqual(getSourceFile(), context.getSourceFile());
    }

    public void cancel() {
        cancelled = true;
        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    public String getConnectionId() {
        return connectionHandlerRef.getConnectionId();
    }
}
