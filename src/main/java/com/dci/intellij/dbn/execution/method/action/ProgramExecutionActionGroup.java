package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ProgramExecutionActionGroup extends DefaultActionGroup {

    public ProgramExecutionActionGroup(DBSchemaObject object) {
        super("Execute", true);
        if (object.getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
            add(new ProgramMethodRunAction((DBProgram) object));
            add(new ProgramMethodDebugAction((DBProgram) object));
        } else {
            add(new MethodRunAction((DBMethod) object));
            add(new MethodDebugAction((DBMethod) object));
        }
    }
}