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
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.frame.XStackFrame;
import com.sun.jdi.Location;

public class DBStatementJdwpDebugProcess extends DBJdwpDebugProcess<StatementExecutionInput> {
    public DBStatementJdwpDebugProcess(@NotNull XDebugSession session, @NotNull DebuggerSession debuggerSession, ConnectionHandler connectionHandler, int tcpPort) {
        super(session, debuggerSession, connectionHandler, tcpPort);
    }

    @Override
    protected void executeTarget() throws SQLException {
        StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(getProject());
        statementExecutionManager.debugExecute(getExecutionProcessor(), getTargetConnection());
    }

    @Override
    public VirtualFile getVirtualFile(XStackFrame stackFrame) {
        try {
            Location location = ((JavaStackFrame) stackFrame).getDescriptor().getLocation();
            if (location != null) {
                int lineNumber = location.lineNumber();
                String sourcePath = location.sourcePath();
                StringTokenizer tokenizer = new StringTokenizer(sourcePath, "\\.");
                String signature = tokenizer.nextToken();
                String programType = tokenizer.nextToken();
                if (programType.equals("Block")) {
                    return getExecutionProcessor().getVirtualFile();
                }
            }
        } catch (Exception e) {
            getConsole().error("Error evaluating susped position: " + e.getMessage());
        }

        return super.getVirtualFile(stackFrame);
    }

    private StatementExecutionProcessor getExecutionProcessor() {
        return getExecutionInput().getExecutionProcessor();
    }

    @NotNull
    @Override
    public String getName() {
        return getExecutionProcessor().getPsiFile().getName();
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return getExecutionProcessor().getPsiFile().getIcon();
    }
}
