package com.dci.intellij.dbn.execution.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.openapi.vfs.VirtualFile;

public class LogOutputRequest {
    private ConnectionHandlerRef connectionHandlerRef;
    private VirtualFile sourceFile;
    private Process process;
    private String text;
    private boolean addHeadline = false;
    private boolean hideEmptyLines = false;
    private boolean cancelled = false;

    public LogOutputRequest(@NotNull ConnectionHandler connectionHandler) {
        this(connectionHandler, null, null);
    }

    public LogOutputRequest(@NotNull ConnectionHandler connectionHandler, @Nullable VirtualFile sourceFile, @Nullable Process process) {
        this.connectionHandlerRef = connectionHandler.getRef();
        this.sourceFile = sourceFile;
        this.process = process;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public boolean isAddHeadline() {
        return addHeadline;
    }

    public void setAddHeadline(boolean addHeadline) {
        this.addHeadline = addHeadline;
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

    public boolean matches(LogOutputRequest request) {
        return getConnectionHandler() == request.getConnectionHandler() &&
                CommonUtil.safeEqual(getSourceFile(), request.getSourceFile());
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
