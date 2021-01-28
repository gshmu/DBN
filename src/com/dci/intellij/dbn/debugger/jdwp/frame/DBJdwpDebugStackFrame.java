package com.dci.intellij.dbn.debugger.jdwp.frame;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.debugger.DBDebugUtil;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugSourcePosition;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugStackFrame;
import com.dci.intellij.dbn.debugger.jdwp.DBJdwpDebugUtil;
import com.dci.intellij.dbn.debugger.jdwp.evaluation.DBJdwpDebuggerEvaluator;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.debugger.engine.JavaStackFrame;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.sun.jdi.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class DBJdwpDebugStackFrame extends DBDebugStackFrame<DBJdwpDebugProcess, DBJdwpDebugValue> implements DBJdwpDebugUtil {
    private long childrenComputed = 0;
    private JavaStackFrame underlyingFrame;

    private final Latent<DBJdwpDebuggerEvaluator> evaluator = Latent.basic(() -> new DBJdwpDebuggerEvaluator(DBJdwpDebugStackFrame.this));

    private final Latent<Location> location = Latent.basic(() -> underlyingFrame == null ? null : underlyingFrame.getDescriptor().getLocation());

    DBJdwpDebugStackFrame(DBJdwpDebugProcess debugProcess, JavaStackFrame underlyingFrame, int index) {
        super(debugProcess, index);
        this.underlyingFrame = underlyingFrame;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        underlyingFrame.computeChildren(node);
/*
        if (System.currentTimeMillis() - childrenComputed > 0){
            System.out.println(System.currentTimeMillis() - childrenComputed);
            underlyingFrame.computeChildren(node);
            childrenComputed = System.currentTimeMillis();
        }
*/
    }

    @Override
    protected XSourcePosition resolveSourcePosition() {
        Location location = getLocation();
        int lineNumber = location == null ? 0 : location.lineNumber() - 1;

        DBJdwpDebugProcess debugProcess = getDebugProcess();
        if (debugProcess.isDeclaredBlock(location) || getOwnerName(location) == null) {
            ExecutionInput executionInput = debugProcess.getExecutionInput();
            if (executionInput instanceof StatementExecutionInput) {
                StatementExecutionInput statementExecutionInput = (StatementExecutionInput) executionInput;
                lineNumber += statementExecutionInput.getExecutableLineNumber();
            }
        }
        return DBDebugSourcePosition.create(getVirtualFile(), lineNumber);
    }

    @Override
    protected VirtualFile resolveVirtualFile() {
        Location location = getLocation();
        return getDebugProcess().getVirtualFile(location);

    }

    public JavaStackFrame getUnderlyingFrame() {
        return underlyingFrame;
    }

    @Override
    @NotNull
    public DBJdwpDebuggerEvaluator getEvaluator() {
        return evaluator.get();
    }


    @Nullable
    public Location getLocation() {
        return location.get();
    }

    @Nullable
    @Override
    protected DBJdwpDebugValue createSuspendReasonDebugValue() {
        return null;
    }

    @NotNull
    @Override
    public DBJdwpDebugValue createDebugValue(String variableName, DBJdwpDebugValue parentValue, List<String> childVariableNames, Icon icon) {
        return new DBJdwpDebugValue(this, parentValue, variableName, childVariableNames, icon);
    }

        @Nullable
    @Override
    public Object getEqualityObject() {
        DBSchemaObject object = DBDebugUtil.getObject(getSourcePosition());
        IdentifierPsiElement subject = getSubject();
        String subjectString = subject == null ? null : subject.getText();

        return object == null ? null : (object.getQualifiedName() + "." + subjectString).toLowerCase();
        //return underlyingFrame.getEqualityObject();
    }
}


