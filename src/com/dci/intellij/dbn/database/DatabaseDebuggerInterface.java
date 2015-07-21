package com.dci.intellij.dbn.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.database.common.debug.BasicOperationInfo;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.database.common.debug.BreakpointOperationInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerSessionInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerVersionInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionBacktraceInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionStatusInfo;
import com.dci.intellij.dbn.database.common.debug.VariableInfo;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.common.DBObjectType;

public interface DatabaseDebuggerInterface extends DatabaseInterface{

    DebuggerSessionInfo initializeSession(Connection connection) throws SQLException;

    void initializeJdwpSession(Connection connection, String host, String port) throws SQLException;
    void disconnectJdwpSession(Connection connection) throws SQLException;

    DebuggerVersionInfo getDebuggerVersion(Connection connection) throws SQLException;

    void enableDebugging(Connection connection) throws SQLException;

    void disableDebugging(Connection connection) throws SQLException;

    void attachSession(String sessionId, Connection connection) throws SQLException;

    void detachSession(Connection connection) throws SQLException;

    DebuggerRuntimeInfo synchronizeSession(Connection connection) throws SQLException;

    BreakpointInfo addProgramBreakpoint(String programOwner, String programName, String programType, int line, Connection connection) throws SQLException;

    BreakpointInfo addSourceBreakpoint(int line, Connection connection) throws SQLException;

    BreakpointOperationInfo removeBreakpoint(int breakpointId, Connection connection) throws SQLException;

    BreakpointOperationInfo enableBreakpoint(int breakpointId, Connection connection) throws SQLException;

    BreakpointOperationInfo disableBreakpoint(int breakpointId, Connection connection) throws SQLException;

    DebuggerRuntimeInfo stepOver(Connection connection) throws SQLException;

    DebuggerRuntimeInfo stepInto(Connection connection) throws SQLException;

    DebuggerRuntimeInfo stepOut(Connection connection) throws SQLException;

    DebuggerRuntimeInfo runToPosition(String programOwner, String programName, String programType, int line, Connection connection) throws SQLException;

    DebuggerRuntimeInfo stopExecution(Connection connection) throws SQLException;

    DebuggerRuntimeInfo resumeExecution(Connection connection) throws SQLException;

    DebuggerRuntimeInfo getRuntimeInfo(Connection connection) throws SQLException;

    ExecutionStatusInfo getExecutionStatusInfo(Connection connection) throws SQLException;

    VariableInfo getVariableInfo(String variableName, Integer frameNumber, Connection connection) throws SQLException;

    BasicOperationInfo setVariableValue(String variableName, Integer frameNumber, String value, Connection connection) throws SQLException;

    ExecutionBacktraceInfo getExecutionBacktraceInfo(Connection connection) throws SQLException;

    String[] getRequiredPrivilegeNames();

    String getDebugConsoleTemplate(CodeStyleCaseSettings settings);

    String getRuntimeEventReason(int code);

    String getJdwpProgramIdentifier(DBObjectType objectType, DBContentType contentType, String qualifiedObjectName);

    String getJdwpTypeName(String typeIdentifier);
}
