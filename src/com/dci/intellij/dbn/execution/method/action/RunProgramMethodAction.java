package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.action.ObjectListShowAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RunProgramMethodAction extends ObjectListShowAction {
    public RunProgramMethodAction(DBProgram program) {
        super("Run...", program);
        getTemplatePresentation().setIcon(Icons.METHOD_EXECUTION_RUN);
    }

    @Nullable
    @Override
    public List<? extends DBObject> getRecentObjectList() {
        DBProgram program = (DBProgram) getSourceObject();
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(program.getProject());
        MethodExecutionHistory executionHistory = methodExecutionManager.getExecutionHistory();
        return executionHistory.getRecentlyExecutedMethods(program);
    }


    public List<DBObject> getObjectList() {
        DBProgram program = (DBProgram) getSourceObject();
        List objects = new ArrayList();
        objects.addAll(program.getProcedures());
        objects.addAll(program.getFunctions());
        return objects;
    }

    public String getTitle() {
        return "Select method to execute";
    }

    public String getEmptyListMessage() {
        DBProgram program = (DBProgram) getSourceObject();
        return "The " + program.getQualifiedNameWithType() + " has no methods to execute.";
    }


    public String getListName() {
       return "executable elements";
   }

    protected AnAction createObjectAction(DBObject object) {
        return new RunMethodAction((DBProgram) getSourceObject(), (DBMethod) object);
    }
}