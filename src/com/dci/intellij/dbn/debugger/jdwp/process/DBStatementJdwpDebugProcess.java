package com.dci.intellij.dbn.debugger.jdwp.process;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.StringTokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.sun.jdi.Location;

public class DBStatementJdwpDebugProcess extends DBJdwpDebugProcess<StatementExecutionInput> {
    public DBStatementJdwpDebugProcess(@NotNull XDebugSession session, @NotNull DebuggerSession debuggerSession, ConnectionHandler connectionHandler, int tcpPort) {
        super(session, debuggerSession, connectionHandler, tcpPort);
    }

    @Override
    protected void executeTarget() throws SQLException {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor();
        if (executionProcessor != null) {
            StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(getProject());
            statementExecutionManager.debugExecute(executionProcessor, getTargetConnection());
        }
    }

    @Override
    @Nullable
    public VirtualFile getVirtualFile(Location location) {
        try {
            if (location != null) {
                String sourcePath = location.sourcePath();
                StringTokenizer tokenizer = new StringTokenizer(sourcePath, "\\.");
                tokenizer.nextToken(); // signature
                String programType = tokenizer.nextToken();
                if (programType.equals("Block")) {
                    StatementExecutionProcessor executionProcessor = getExecutionProcessor();
                    if (executionProcessor != null) {
                        return executionProcessor.getVirtualFile();
                    }
                }
            }
        } catch (Exception e) {
            getConsole().error("Error evaluating suspend position: " + e.getMessage());
        }

        return super.getVirtualFile(location);
    }

    @Nullable
    private StatementExecutionProcessor getExecutionProcessor() {
        StatementExecutionInput executionInput = getExecutionInput();
        return executionInput == null ? null : executionInput.getExecutionProcessor();
    }

    @NotNull
    @Override
    public String getName() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor();
        if (executionProcessor != null) {
            return executionProcessor.getPsiFile().getName();
        }
        return "Debug Process";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor();
        if (executionProcessor != null) {
            return executionProcessor.getPsiFile().getIcon();
        }
        return null;
    }
}
