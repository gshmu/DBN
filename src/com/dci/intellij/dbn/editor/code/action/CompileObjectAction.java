package com.dci.intellij.dbn.editor.code.action;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;

public class CompileObjectAction extends AbstractSourceCodeEditorAction {
    public CompileObjectAction() {
        super("", "", Icons.OBEJCT_COMPILE);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        FileEditor fileEditor = getFileEditor(e);
        if (sourceCodeFile != null && fileEditor != null) {
            Project project = sourceCodeFile.getProject();
            if (project != null) {
                DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
                CompilerSettings compilerSettings = getCompilerSettings(project);
                DBContentType contentType = sourceCodeFile.getContentType();
                CompilerAction compilerAction = new CompilerAction(CompilerActionSource.COMPILE, contentType, sourceCodeFile, fileEditor);
                compilerManager.compileInBackground(sourceCodeFile.getObject(), compilerSettings.getCompileType(), compilerAction);
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourceCodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        if (sourceCodeFile == null) {
            presentation.setEnabled(false);
        } else {

            DBSchemaObject schemaObject = sourceCodeFile.getObject();
            if (schemaObject.getProperties().is(DBObjectProperty.COMPILABLE) && DatabaseFeature.OBJECT_INVALIDATION.isSupported(schemaObject)) {
                CompilerSettings compilerSettings = getCompilerSettings(schemaObject.getProject());
                CompileType compileType = compilerSettings.getCompileType();
                DBObjectStatusHolder status = schemaObject.getStatus();
                DBContentType contentType = sourceCodeFile.getContentType();

                boolean isDebug = compileType == CompileType.DEBUG;
                if (compileType == CompileType.KEEP) {
                    isDebug = status.is(contentType, DBObjectStatus.DEBUG);
                }

                boolean isPresent = status.is(contentType, DBObjectStatus.PRESENT);
                boolean isValid = status.is(contentType, DBObjectStatus.VALID);
                boolean isModified = sourceCodeFile.isModified();

                boolean isCompiling = status.is(contentType, DBObjectStatus.COMPILING);
                boolean isEnabled = !isModified && isPresent && !isCompiling && (compilerSettings.alwaysShowCompilerControls() || !isValid /*|| isDebug != isDebugActive*/);

                presentation.setEnabled(isEnabled);
                String text =
                        contentType == DBContentType.CODE_SPEC ? "Compile spec" :
                                contentType == DBContentType.CODE_BODY ? "Compile body" : "Compile";

                if (isDebug) text = text + " (Debug)";
                if (compileType == CompileType.ASK) text = text + "...";

                presentation.setVisible(true);
                presentation.setText(text);

                Icon icon = isDebug ?
                        CompileType.DEBUG.getIcon() :
                        CompileType.NORMAL.getIcon();
                presentation.setIcon(icon);
            } else {
                presentation.setVisible(false);
            }
        }
    }

    private static CompilerSettings getCompilerSettings(Project project) {
        return OperationSettings.getInstance(project).getCompilerSettings();
    }
}
