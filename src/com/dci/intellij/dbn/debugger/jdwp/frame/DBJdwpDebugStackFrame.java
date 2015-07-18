package com.dci.intellij.dbn.debugger.jdwp.frame;

import javax.swing.Icon;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugStackFrame;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugValue;
import com.dci.intellij.dbn.debugger.jdbc.evaluation.DBJdbcDebuggerEvaluator;
import com.dci.intellij.dbn.debugger.jdwp.DBJdwpDebugProcess;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.sun.jdi.Location;

public class DBJdwpDebugStackFrame extends DBDebugStackFrame<DBJdwpDebugProcess>{
    private XStackFrame underlyingFrame;
    private DBJdbcDebuggerEvaluator evaluator;

    private LazyValue<Location> location = new SimpleLazyValue<Location>() {
        @Override
        protected Location load() {
            return ((JavaStackFrame) underlyingFrame).getDescriptor().getLocation();
        }
    };

    private LazyValue<VirtualFile> virtualFile = new SimpleLazyValue<VirtualFile>() {
        @Override
        protected VirtualFile load() {
            return getDebugProcess().getVirtualFile(underlyingFrame);
        }
    };

    public DBJdwpDebugStackFrame(DBJdwpDebugProcess debugProcess, XStackFrame underlyingFrame, int index) {
        super(debugProcess, index);
        this.underlyingFrame = underlyingFrame;
    }

    @Override
    protected XSourcePosition computeSourcePosition() {
        Location location = getLocation();
        int lineNumber = location == null ? 0 : location.lineNumber() - 1;

        DBJdwpDebugProcess debugProcess = getDebugProcess();
        String ownerName = debugProcess.getOwnerName(underlyingFrame);
        if (ownerName == null) {
            ExecutionInput executionInput = debugProcess.getExecutionInput();
            if (executionInput instanceof StatementExecutionInput) {
                StatementExecutionInput statementExecutionInput = (StatementExecutionInput) executionInput;
                lineNumber += statementExecutionInput.getExecutableLineNumber();
            }
        }
        return XSourcePositionImpl.create(getVirtualFile(), lineNumber);
    }

    public XStackFrame getUnderlyingFrame() {
    return underlyingFrame;
}

    @Override
    public XDebuggerEvaluator getEvaluator() {
/*
        if (evaluator == null) {
            evaluator = new DBProgramDebuggerEvaluator(this);
        }
*/
        return evaluator;
    }


    public Location getLocation() {
        return location.get();
    }


    public VirtualFile getVirtualFile() {
        return virtualFile.get();
    }

    @Nullable
    @Override
    protected DBDebugValue createSuspendReasonDebugValue() {
        return null;
    }

    @NotNull
    @Override
    protected DBJdwpDebugValue createDebugValue(String variableName, Set<String> childVariableNames, Icon icon) {
        return new DBJdwpDebugValue(this, null, variableName, childVariableNames, icon);
    }

        @Nullable
    @Override
    public Object getEqualityObject() {
        DBSchemaObject object = DBDebugUtil.getObject(getSourcePosition());
        return object == null ? null : object.getQualifiedName();
    }
}


