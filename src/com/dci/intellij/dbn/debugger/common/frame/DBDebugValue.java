package com.dci.intellij.dbn.debugger.common.frame;

import javax.swing.Icon;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.debugger.common.evaluation.DBDebuggerEvaluator;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;

public abstract class DBDebugValue<T extends DBDebugStackFrame> extends XNamedValue implements Comparable<DBDebugValue>{
    protected String value;
    protected String type;
    protected Icon icon;
    protected Set<String> childVariableNames;

    private T stackFrame;
    private DBDebugValue parentValue;

    protected DBDebugValue(T stackFrame, @NotNull String variableName, @Nullable Set<String> childVariableNames, @Nullable DBDebugValue parentValue, @Nullable Icon icon) {
        super(variableName);
        this.stackFrame = stackFrame;
        this.parentValue = parentValue;
        if (icon == null) {
            icon = parentValue == null ?
                    Icons.DBO_VARIABLE :
                    Icons.DBO_ATTRIBUTE;
        }
        this.icon = icon;
        this.childVariableNames = childVariableNames;
    }

    @Override
    public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
        // enabling this will show always variables as changed
        //node.setPresentation(icon, null, "", childVariableNames != null);
        new SimpleBackgroundTask("load variable value") {
            @Override
            protected void execute() {
                DBDebuggerEvaluator<? extends DBDebugStackFrame, DBDebugValue> evaluator = getStackFrame().getEvaluator();
                evaluator.computePresentation(DBDebugValue.this, node, place);
            }
        }.start();
    }


    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (childVariableNames != null) {
            for (String childVariableName : childVariableNames) {
                childVariableName = childVariableName.substring(getVariableName().length() + 1);
                XValueChildrenList debugValueChildren = new XValueChildrenList();
                DBDebugValue value = stackFrame.createDebugValue(childVariableName, this, null, null);
                debugValueChildren.add(value);
                node.addChildren(debugValueChildren, true);
            }
        } else {
            super.computeChildren(node);
        }
    }

    public T getStackFrame() {
        return stackFrame;
    }

    public DBDebugProcess getDebugProcess() {
        return stackFrame.getDebugProcess();
    }

    public DBDebugValue getParentValue() {
        return parentValue;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVariableName() {
        return getName();
    }


    @Override
    public int compareTo(@NotNull DBDebugValue remote) {
        return getName().compareTo(remote.getName());
    }

    public Set<String> getChildVariableNames() {
        return childVariableNames;
    }

    public Icon getIcon() {
        return icon;
    }
}

