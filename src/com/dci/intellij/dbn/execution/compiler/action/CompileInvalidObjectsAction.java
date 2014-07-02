package com.dci.intellij.dbn.execution.compiler.action;

import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class CompileInvalidObjectsAction extends AnAction {
    private DBSchema schema;
    public CompileInvalidObjectsAction(DBSchema schema) {
        super("Compile invalid objects");
        this.schema = schema;
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = schema.getProject();
        DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
        compilerManager.compileInvalidObjects(schema, getCompilerSettings(project).getCompileType());
    }

    @Override
    public void update(AnActionEvent e) {
        CompileType compileType = getCompilerSettings(schema.getProject()).getCompileType();
        String text = "Compile invalid objects";
        if (compileType == CompileType.DEBUG) text = text + " (Debug)";
        if (compileType == CompileType.ASK) text = text + "...";

        e.getPresentation().setText(text);
    }

    private CompilerSettings getCompilerSettings(Project project) {
        return ExecutionEngineSettings.getInstance(project).getCompilerSettings();
    }
}