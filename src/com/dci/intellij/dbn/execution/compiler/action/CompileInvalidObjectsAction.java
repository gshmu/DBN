package com.dci.intellij.dbn.execution.compiler.action;

import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CompileInvalidObjectsAction extends AnAction {
    private DBSchema schema;
    public CompileInvalidObjectsAction(DBSchema schema) {
        super("Compile invalid objects");
        this.schema = schema;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = schema.getProject();
        DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
        compilerManager.compileInvalidObjects(schema, getCompilerSettings(project).getCompileTypeOption());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        CompileTypeOption compileType = getCompilerSettings(schema.getProject()).getCompileTypeOption();
        String text = "Compile Invalid Objects";
        if (compileType == CompileTypeOption.DEBUG) text = text + " (Debug)";
        if (compileType == CompileTypeOption.ASK) text = text + "...";

        e.getPresentation().setText(text);
    }

    private static CompilerSettings getCompilerSettings(Project project) {
        return OperationSettings.getInstance(project).getCompilerSettings();
    }
}