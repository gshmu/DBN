package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RunMethodAction extends AbstractSourceCodeEditorAction {
    public RunMethodAction() {
        super("Run Method", "", Icons.METHOD_EXECUTION_RUN);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        FileEditor fileEditor = getFileEditor(e);
        if (sourceCodeFile != null && fileEditor != null) {
            Project project = sourceCodeFile.getProject();
            if (project != null) {
                DBMethod method = (DBMethod) sourceCodeFile.getObject();
                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                executionManager.startMethodExecution(method, DBDebuggerType.NONE);
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        boolean visible = false;
        if (sourceCodeFile != null) {
            DBSchemaObject schemaObject = sourceCodeFile.getObject();
            if (schemaObject.getObjectType().matches(DBObjectType.METHOD)) {
                visible = true;
            }
        }

        presentation.setVisible(visible);
        presentation.setText("Run Method");
    }

    private static CompilerSettings getCompilerSettings(Project project) {
        return OperationSettings.getInstance(project).getCompilerSettings();
    }
}
