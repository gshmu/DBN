package com.dci.intellij.dbn.execution.compiler.action;

import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CompileObjectAction extends DumbAwareAction {
    private DBObjectRef<DBSchemaObject> objectRef;
    private DBContentType contentType;
    private CompileType compileType;

    public CompileObjectAction(DBSchemaObject object, DBContentType contentType, CompileType compileType) {
        super("Compile");
        this.objectRef = DBObjectRef.from(object);
        this.contentType = contentType;
        this.compileType = compileType;
    }

    public DBSchemaObject getObject() {
        return DBObjectRef.ensure(objectRef);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSchemaObject object = getObject();
        DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(object.getProject());
        CompilerAction compilerAction = new CompilerAction(CompilerActionSource.COMPILE, contentType);
        compilerManager.compileInBackground(object, compileType, compilerAction);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DBSchemaObject object = getObject();
        Presentation presentation = e.getPresentation();

        DBObjectStatusHolder status = object.getStatus();

        boolean isPresent = status.is(contentType, DBObjectStatus.PRESENT);
        boolean isCompiling = status.is(contentType, DBObjectStatus.COMPILING);
        boolean isEnabled = isPresent && !isCompiling /*&& (compilerSettings.alwaysShowCompilerControls() || !isValid)*/;

        presentation.setEnabled(isEnabled);

        String text = "Compile " + object.getObjectType().getName();
        if (compileType == CompileType.DEBUG) {
            text += " (debug)";
        }
        presentation.setText(text);
    }

    private static CompilerSettings getCompilerSettings(Project project) {
        return OperationSettings.getInstance(project).getCompilerSettings();
    }
}
