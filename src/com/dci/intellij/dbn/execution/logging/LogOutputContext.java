package com.dci.intellij.dbn.execution.logging;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LogOutputContext {
    public enum Status{
        NEW,
        ACTIVE,
        FINISHED,    // finished normally (or with error)
        STOPPED,     // interrupted by user
        CLOSED      // cancelled completely (console closed)
    }
    private ConnectionHandlerRef connectionHandlerRef;
    private WeakRef<VirtualFile> sourceFile;
    private WeakRef<Process> process;
    private Status status = Status.NEW;
    private boolean hideEmptyLines = false;

    public LogOutputContext(@NotNull ConnectionHandler connectionHandler) {
        this(connectionHandler, null, null);
    }

    public LogOutputContext(@NotNull ConnectionHandler connectionHandler, @Nullable VirtualFile sourceFile, @Nullable Process process) {
        this.connectionHandlerRef = connectionHandler.getRef();
        this.sourceFile = WeakRef.from(sourceFile);
        this.process = WeakRef.from(process);
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.ensure();
    }

    @Nullable
    public VirtualFile getSourceFile() {
        return WeakRef.get(sourceFile);
    }

    @Nullable
    public Process getProcess() {
        return WeakRef.get(process);
    }

    public void setProcess(Process process) {
        this.process = WeakRef.from(process);
    }

    public boolean isProcessAlive() {
        Process process = getProcess();
        if (process != null) {
            try {
                process.exitValue();
            } catch(IllegalThreadStateException e) {
                return true;
            }
        }
        return false;
    }

    public boolean isHideEmptyLines() {
        return hideEmptyLines;
    }

    public void setHideEmptyLines(boolean hideEmptyLines) {
        this.hideEmptyLines = hideEmptyLines;
    }

    public boolean matches(LogOutputContext context) {
        return getConnectionHandler() == context.getConnectionHandler() &&
                CommonUtil.safeEqual(getSourceFile(), context.getSourceFile());
    }

    public void start() {
        status = Status.ACTIVE;
    }

    public void finish() {
        if (isActive()) {
            status = Status.FINISHED;
        }
        destroyProcess();
    }


    public void stop() {
        if (isActive()) {
            status = Status.STOPPED;
        }
        destroyProcess();
    }


    public void close() {
        status = Status.CLOSED;
        destroyProcess();
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean isClosed() {
        return status == Status.CLOSED;
    }

    public boolean isStopped() {
        return status == Status.STOPPED;
    }


    private void destroyProcess() {
        Process process = getProcess();
        if (process != null) {
            process.destroy();
        }
    }

    public ConnectionId getConnectionId() {
        return connectionHandlerRef.getConnectionId();
    }
}
