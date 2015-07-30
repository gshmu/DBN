package com.dci.intellij.dbn.debugger.jdwp.process;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfig;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.xdebugger.XDebugSession;

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
    protected void registerDefaultBreakpoint() {
        DBStatementRunConfig runConfiguration = (DBStatementRunConfig) getSession().getRunProfile();
        if (runConfiguration != null) {
            List<DBMethod> methods = runConfiguration.getMethods();
            if (methods.size() > 0) {
                getBreakpointHandler().registerDefaultBreakpoint(methods.get(0));
            }
        }
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
